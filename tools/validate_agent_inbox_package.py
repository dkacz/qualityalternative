#!/usr/bin/env python3
"""Validate a Quality Alternative Agent Inbox package folder."""

from __future__ import annotations

import argparse
import hashlib
import json
import re
import sys
from pathlib import Path
from typing import Any


MAX_PACKAGE_FILES = 8
MAX_MANIFEST_BYTES = 64 * 1024
MAX_CONTENT_BYTES = 10 * 1024 * 1024
MAX_IMAGE_ATTACHMENTS = 6
MAX_IMAGE_BYTES = 5 * 1024 * 1024
MAX_TOTAL_IMAGE_BYTES = 15 * 1024 * 1024

ALLOWED_TOPICS = {
    "ATTENTION",
    "PRACTICAL",
    "BODY",
    "NATURE",
    "HISTORY_CULTURE",
    "ESSAYS",
    "PHILOSOPHY",
    "SCIENCE",
    "DESIGN",
    "POETRY",
    "HISTORY",
    "TECH",
    "FICTION",
    "CLIMATE",
    "ECONOMICS",
    "FOOD",
    "ARCHITECTURE",
    "CREATIVITY",
    "PSYCHOLOGY",
    "OTHER",
}

CONTENT_EXTENSIONS = {".md": "MARKDOWN", ".markdown": "MARKDOWN", ".epub": "EPUB"}
IMAGE_EXTENSIONS = {".png", ".jpg", ".jpeg", ".webp", ".gif", ".bmp"}
SHA256_RE = re.compile(r"^[0-9a-f]{64}$")
SAFE_SEGMENT_RE = re.compile(r"[^A-Za-z0-9._-]+")


def safe_agent_inbox_file_segment(value: str) -> str:
    cleaned = SAFE_SEGMENT_RE.sub("-", value.strip()).strip("-.")
    return cleaned or "document"


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def normalized_string(value: Any) -> str:
    return value.strip() if isinstance(value, str) else ""


def validate_package(package_dir: Path) -> list[str]:
    errors: list[str] = []
    if not package_dir.exists():
        return [f"Package path does not exist: {package_dir}"]
    if not package_dir.is_dir():
        return [f"Package path is not a directory: {package_dir}"]

    entries = sorted(package_dir.iterdir(), key=lambda path: path.name.lower())
    files = [entry for entry in entries if entry.is_file() and not entry.is_symlink()]
    non_files = [entry for entry in entries if not entry.is_file() or entry.is_symlink()]
    if non_files:
        errors.append("Package must contain only direct files; nested folders and symlinks are not supported.")
    if len(files) > MAX_PACKAGE_FILES:
        errors.append(f"Package has {len(files)} files; maximum is {MAX_PACKAGE_FILES}.")

    manifest_path = package_dir / "manifest.json"
    if not manifest_path.is_file() or manifest_path.is_symlink():
        errors.append("Package is missing manifest.json.")
        return errors
    if manifest_path.stat().st_size > MAX_MANIFEST_BYTES:
        errors.append(f"manifest.json is larger than {MAX_MANIFEST_BYTES} bytes.")

    try:
        manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    except UnicodeDecodeError:
        errors.append("manifest.json must be UTF-8.")
        return errors
    except json.JSONDecodeError as exc:
        errors.append(f"manifest.json is malformed JSON: {exc.msg}.")
        return errors
    if not isinstance(manifest, dict):
        errors.append("manifest.json must be a JSON object.")
        return errors

    if manifest.get("schemaVersion") != 1:
        errors.append("schemaVersion must be 1.")

    title = normalized_string(manifest.get("title"))
    if not title:
        errors.append("title must be a non-empty string.")

    topics = manifest.get("topics")
    if not isinstance(topics, list) or not topics:
        errors.append("topics must be a non-empty array.")
    else:
        for topic in topics:
            raw_topic = normalized_string(topic).upper()
            if raw_topic not in ALLOWED_TOPICS:
                errors.append(f"Unknown topic: {topic!r}.")

    content_file_name = normalized_string(manifest.get("contentFile"))
    if not content_file_name:
        errors.append("contentFile must be a non-empty string.")
        content_path = None
    elif "/" in content_file_name or "\\" in content_file_name or ".." in content_file_name:
        errors.append("contentFile must be a safe filename, not a path.")
        content_path = package_dir / content_file_name
    else:
        content_path = package_dir / content_file_name
        if not content_path.is_file():
            errors.append(f"contentFile is not present in the package: {content_file_name}.")

    format_value = normalized_string(manifest.get("format")).upper()
    if format_value not in {"MARKDOWN", "EPUB"}:
        errors.append("format must be MARKDOWN or EPUB.")

    expected_format = CONTENT_EXTENSIONS.get(Path(content_file_name).suffix.lower())
    if content_file_name and expected_format is None:
        errors.append("contentFile must end with .md, .markdown, or .epub.")
    elif expected_format is not None and format_value and expected_format != format_value:
        errors.append(f"format {format_value} does not match contentFile extension {Path(content_file_name).suffix}.")

    if normalized_string(manifest.get("rightsClass")).upper() != "USER_PRIVATE":
        errors.append("rightsClass must be USER_PRIVATE.")

    priority = normalized_string(manifest.get("priority")).lower()
    if priority not in {"", "normal", "high"}:
        errors.append("priority must be normal, high, or omitted.")

    content_files = [
        file
        for file in files
        if file.name != "manifest.json" and file.suffix.lower() in CONTENT_EXTENSIONS
    ]
    if len(content_files) != 1:
        errors.append("Package must contain exactly one Markdown or EPUB content file.")

    if content_path is not None and content_path.is_file():
        content_size = content_path.stat().st_size
        if content_size > MAX_CONTENT_BYTES:
            errors.append(f"Content file is {content_size} bytes; maximum is {MAX_CONTENT_BYTES}.")
        actual_sha = sha256(content_path)
        manifest_sha = normalized_string(manifest.get("documentSha256"))
        if manifest_sha:
            if not SHA256_RE.match(manifest_sha):
                errors.append("documentSha256 must be 64 lowercase hex characters.")
            elif manifest_sha != actual_sha:
                errors.append("documentSha256 does not match contentFile bytes.")
    else:
        actual_sha = None

    extra_files = [
        file
        for file in files
        if file.name != "manifest.json" and file.name != content_file_name
    ]
    if format_value == "EPUB" and extra_files:
        errors.append("EPUB packages cannot include sidecar files.")
    elif format_value == "MARKDOWN":
        image_files = [file for file in extra_files if file.suffix.lower() in IMAGE_EXTENSIONS]
        unsupported = [file.name for file in extra_files if file.suffix.lower() not in IMAGE_EXTENSIONS]
        if unsupported:
            errors.append(f"Unsupported extra files: {', '.join(unsupported)}.")
        if len(image_files) > MAX_IMAGE_ATTACHMENTS:
            errors.append(f"Markdown package has {len(image_files)} image attachments; maximum is {MAX_IMAGE_ATTACHMENTS}.")
        display_names = [file.name.lower() for file in image_files]
        if len(set(display_names)) != len(display_names):
            errors.append("Markdown image attachment names must be unique case-insensitively.")
        safe_names = [safe_agent_inbox_file_segment(file.name).lower() for file in image_files]
        if len(set(safe_names)) != len(safe_names):
            errors.append("Markdown image attachment names collide after safe-name cleanup.")
        total_image_bytes = sum(file.stat().st_size for file in image_files)
        if total_image_bytes > MAX_TOTAL_IMAGE_BYTES:
            errors.append(f"Markdown image attachments total {total_image_bytes} bytes; maximum is {MAX_TOTAL_IMAGE_BYTES}.")
        for image_file in image_files:
            image_size = image_file.stat().st_size
            if image_size > MAX_IMAGE_BYTES:
                errors.append(f"Image attachment {image_file.name} is {image_size} bytes; maximum is {MAX_IMAGE_BYTES}.")
    elif extra_files:
        errors.append("Extra files are only supported for MARKDOWN image sidecars.")

    if actual_sha:
        print(f"contentSha256={actual_sha}")
    return errors


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("package_dir", type=Path, help="Path to one Agent Inbox package folder.")
    args = parser.parse_args()

    errors = validate_package(args.package_dir)
    if errors:
        print("FAIL: Agent Inbox package is invalid.", file=sys.stderr)
        for error in errors:
            print(f"- {error}", file=sys.stderr)
        return 1
    print("PASS: Agent Inbox package is valid.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
