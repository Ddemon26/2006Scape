import os
import re
import sys
import argparse

parser = argparse.ArgumentParser(description='Populate class documentation')
parser.add_argument('--docs-dir', default='docs/Server/classes',
                    help='Directory of markdown class docs')
parser.add_argument('--src-root', default='2006Scape Server/src/main/java',
                    help='Root directory of Java sources')
parser.add_argument('files', nargs='*', help='Optional list of markdown files')
args = parser.parse_args()

DOCS_DIR = args.docs_dir
SRC_ROOT = args.src_root

def find_java(class_name):
    target = class_name + '.java'
    for root, _, files in os.walk(SRC_ROOT):
        if target in files:
            return os.path.join(root, target)
    return None

def parse_java(java_path):
    package = None
    javadoc = None
    class_decl = ''
    methods = []
    with open(java_path) as f:
        lines = f.readlines()
    start = 0
    for i, line in enumerate(lines):
        stripped = line.strip()
        if stripped.startswith('package '):
            package = stripped[8:].rstrip(';')
        if stripped.startswith('/**'):
            comment = []
            j = i + 1
            while j < len(lines) and '*/' not in lines[j]:
                comment.append(lines[j].strip().lstrip('*').strip())
                j += 1
            javadoc = ' '.join(comment).strip()
        if re.search(r'\b(class|interface|enum)\b', line):
            class_decl = stripped
            start = i + 1
            break
    method_pattern = re.compile(r'^\s*public\s+.*\(.*\)')
    for line in lines[start:]:
        if method_pattern.match(line):
            methods.append(re.sub(r'\s*\{\s*$', '', line.strip()))
    return package, javadoc, class_decl, methods

def default_description(class_name):
    if class_name.endswith('Handler'):
        subject = re.sub(r'([a-z])([A-Z])', r'\1 \2', class_name[:-7]).lower()
        return f'Handles {subject} related functionality.'
    if class_name.endswith('Commands'):
        subject = re.sub(r'([a-z])([A-Z])', r'\1 \2', class_name[:-8]).lower()
        return f'Discord commands for {subject}.'
    if class_name.endswith('Agility'):
        return 'Gameplay logic related to the Agility skill.'
    if class_name.endswith('Constants'):
        subject = re.sub(r'([a-z])([A-Z])', r'\1 \2', class_name[:-9]).lower()
        return f'Static constants used by {subject}.'
    base = re.sub(r'([a-z])([A-Z])', r'\1 \2', class_name)
    return f'{base} helper class.'

files = args.files
if not files:
    files = [os.path.join(DOCS_DIR, name)
             for name in os.listdir(DOCS_DIR)
             if name.endswith('.md')]

if not files:
    print('No documentation files to update.')
    sys.exit(0)

for md in files:
    class_name = os.path.splitext(os.path.basename(md))[0]
    java_path = find_java(class_name)
    if not java_path:
        continue
    package, javadoc, class_decl, methods = parse_java(java_path)
    rel_java_path = os.path.relpath(java_path)

    lines = [f'# {class_name}', '']
    if package:
        lines.append(f'Package `{package}`.')
        lines.append('')
    lines.append(f'Defined in [`{rel_java_path}`]({rel_java_path}).')
    lines.append('')
    description = javadoc if javadoc else default_description(class_name)
    if description:
        lines.append(description)
        lines.append('')
    snippet = []
    if class_decl:
        snippet.append(class_decl)
    snippet.extend(methods)
    if snippet:
        lines.append('```java')
        lines.extend(snippet)
        lines.append('```')
    with open(md, 'w') as f:
        f.write('\n'.join(lines) + '\n')
