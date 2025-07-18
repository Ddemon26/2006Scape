#!/usr/bin/env python3
"""
Generate source code links for 2006Scape documentation.
This script scans the source code and updates documentation files with proper GitHub links.
"""

import os
import re
import json
from pathlib import Path

# Configuration
GITHUB_BASE_URL = "https://github.com/2006-Scape/2006rebotted/blob/main"
SERVER_SOURCE_PATH = "2006Scape Server/src/main/java"
CLIENT_SOURCE_PATH = "2006Scape Client/src/main/java"
DOCS_SERVER_PATH = "docs/Server/classes"
DOCS_CLIENT_PATH = "docs/Client/classes"

def scan_java_files(source_path):
    """Scan Java files and extract class information."""
    java_files = []
    source_dir = Path(source_path)
    
    if not source_dir.exists():
        print(f"Warning: Source directory {source_path} does not exist")
        return java_files
    
    for java_file in source_dir.rglob("*.java"):
        relative_path = java_file.relative_to(source_dir.parent.parent.parent)
        class_name = java_file.stem
        
        # Read file to extract package and class info
        try:
            with open(java_file, 'r', encoding='utf-8', errors='ignore') as f:
                content = f.read()
                
            # Extract package
            package_match = re.search(r'package\s+([^;]+);', content)
            package = package_match.group(1) if package_match else ""
            
            # Extract class declaration
            class_match = re.search(r'(public\s+)?(abstract\s+)?(final\s+)?(class|interface|enum)\s+' + class_name, content)
            class_type = class_match.group(4) if class_match else "class"
            
            # Extract methods (simplified)
            methods = re.findall(r'(public|private|protected)\s+[^{]*?\s+(\w+)\s*\([^)]*\)', content)
            
            java_files.append({
                'name': class_name,
                'path': str(relative_path),
                'package': package,
                'type': class_type,
                'methods': [method[1] for method in methods[:10]],  # Limit to first 10 methods
                'github_url': f"{GITHUB_BASE_URL}/{relative_path}".replace("\\", "/")
            })
            
        except Exception as e:
            print(f"Error processing {java_file}: {e}")
    
    return java_files

def update_documentation_file(doc_path, class_info):
    """Update a documentation file with proper source links and enhanced content."""
    if not os.path.exists(doc_path):
        print(f"Creating new documentation file: {doc_path}")
        create_new_doc_file(doc_path, class_info)
        return
    
    try:
        with open(doc_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Update GitHub link
        old_link_pattern = r'\[`[^`]+`\]\([^)]+\)'
        new_link = f"[`{class_info['path']}`]({class_info['github_url']})"
        
        if old_link_pattern in content:
            content = re.sub(old_link_pattern, new_link, content)
        else:
            # Add link after the title
            title_line = f"# {class_info['name']}"
            if title_line in content:
                content = content.replace(
                    title_line,
                    f"{title_line}\n\nSource: {new_link}"
                )
        
        # Add package information if missing
        if class_info['package'] and f"Package `{class_info['package']}`" not in content:
            title_line = f"# {class_info['name']}"
            if title_line in content:
                content = content.replace(
                    title_line,
                    f"{title_line}\n\nPackage: `{class_info['package']}`"
                )
        
        with open(doc_path, 'w', encoding='utf-8') as f:
            f.write(content)
            
        print(f"Updated: {doc_path}")
        
    except Exception as e:
        print(f"Error updating {doc_path}: {e}")

def create_new_doc_file(doc_path, class_info):
    """Create a new documentation file for a class."""
    os.makedirs(os.path.dirname(doc_path), exist_ok=True)
    
    content = f"""# {class_info['name']}

Package: `{class_info['package']}`

Source: [`{class_info['path']}`]({class_info['github_url']})

{class_info['type'].title()} in the 2006Scape {'server' if 'Server' in class_info['path'] else 'client'}.

## Methods

{chr(10).join(f"- `{method}()`" for method in class_info['methods'][:10])}

## Usage

This {class_info['type']} is part of the {'server-side game logic' if 'Server' in class_info['path'] else 'client-side rendering and UI system'}.

## Related Classes

See the [{'Server' if 'Server' in class_info['path'] else 'Client'} Classes](index.md) for related components.
"""
    
    with open(doc_path, 'w', encoding='utf-8') as f:
        f.write(content)
    
    print(f"Created: {doc_path}")

def update_class_index(docs_path, java_files, title):
    """Update the class index file with all classes."""
    index_path = os.path.join(docs_path, "index.md")
    
    # Sort classes alphabetically
    sorted_classes = sorted(java_files, key=lambda x: x['name'])
    
    content = f"# {title}\n\n"
    
    for class_info in sorted_classes:
        content += f"- [{class_info['name']}]({class_info['name']}.md)\n"
    
    content += f"\n## Source Code\n\nAll source code is available on [GitHub]({GITHUB_BASE_URL}).\n"
    
    with open(index_path, 'w', encoding='utf-8') as f:
        f.write(content)
    
    print(f"Updated index: {index_path}")

def main():
    """Main function to process all source files and update documentation."""
    print("Scanning source code and updating documentation links...")
    
    # Process server classes
    print("\nProcessing server classes...")
    server_files = scan_java_files(SERVER_SOURCE_PATH)
    print(f"Found {len(server_files)} server classes")
    
    for class_info in server_files:
        doc_path = os.path.join(DOCS_SERVER_PATH, f"{class_info['name']}.md")
        update_documentation_file(doc_path, class_info)
    
    update_class_index(DOCS_SERVER_PATH, server_files, "Server Classes")
    
    # Process client classes
    print("\nProcessing client classes...")
    client_files = scan_java_files(CLIENT_SOURCE_PATH)
    print(f"Found {len(client_files)} client classes")
    
    for class_info in client_files:
        doc_path = os.path.join(DOCS_CLIENT_PATH, f"{class_info['name']}.md")
        update_documentation_file(doc_path, class_info)
    
    update_class_index(DOCS_CLIENT_PATH, client_files, "Client Classes")
    
    # Generate summary
    print(f"\nSummary:")
    print(f"- Server classes: {len(server_files)}")
    print(f"- Client classes: {len(client_files)}")
    print(f"- Total classes documented: {len(server_files) + len(client_files)}")
    
    # Save class information for other tools
    all_classes = {
        'server': server_files,
        'client': client_files,
        'generated_at': str(Path.cwd()),
        'github_base': GITHUB_BASE_URL
    }
    
    with open('docs/class_index.json', 'w') as f:
        json.dump(all_classes, f, indent=2)
    
    print("\nDocumentation linking complete!")

if __name__ == "__main__":
    main()