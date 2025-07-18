import os
import argparse

class DocScanner:
    """Utility to check which documentation files have been populated."""

    def __init__(self, docs_dir='docs/Server/classes'):
        self.docs_dir = docs_dir

    def _is_processed(self, path: str) -> bool:
        try:
            with open(path) as f:
                for line in f:
                    if line.startswith('Defined in') or line.startswith('Source:'):
                        return True
        except FileNotFoundError:
            pass
        return False

    def is_processed_file(self, path: str) -> bool:
        return self._is_processed(path)

    def processed(self):
        result = set()
        for name in os.listdir(self.docs_dir):
            if not name.endswith('.md'):
                continue
            md_path = os.path.join(self.docs_dir, name)
            if self._is_processed(md_path):
                result.add(os.path.splitext(name)[0])
        return result

    def unprocessed(self):
        all_classes = {
            os.path.splitext(f)[0]
            for f in os.listdir(self.docs_dir)
            if f.endswith('.md')
        }
        return sorted(all_classes - self.processed())

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='List docs missing metadata')
    parser.add_argument('--docs-dir', default='docs/Server/classes',
                        help='Directory containing markdown class docs')
    args = parser.parse_args()

    scanner = DocScanner(args.docs_dir)
    for class_name in scanner.unprocessed():
        print(class_name)
