class NavigationManager {
    constructor() {
        this.navigation = [];
    }

    async load() {
        try {
            const response = await fetch('documents/navigation.json');
            if (response.ok) {
                this.navigation = await response.json();
            } else {
                this.generateDefaultNavigation();
            }
        } catch (error) {
            console.error('Failed to load navigation:', error);
            this.generateDefaultNavigation();
        }
    }

    generateDefaultNavigation() {
        this.navigation = [
            {
                title: "Getting Started",
                items: [
                    { title: "Introduction", path: "documents/introduction.md" },
                    { title: "Installation", path: "documents/installation.md" },
                    { title: "Quick Start", path: "documents/quick-start.md" }
                ]
            },
            {
                title: "Language Reference",
                items: [
                    { title: "Lexical Analysis", path: "documents/lexical.md" },
                ]
            },
            {
                title: "API Reference",
                items: [
                    { title: "Overview", path: "documents/api/overview.md" },
                    { title: "Authentication", path: "documents/api/authentication.md" },
                    { title: "API Keys", path: "documents/api/auth/api-keys.md" }
                ]
            },
            {
                title: "Guides",
                items: [
                    { title: "Basic Usage", path: "documents/guides/basic-usage.md" },
                    { title: "Advanced Features", path: "documents/guides/advanced-features.md" }
                ]
            },
            {
                title: "Examples",
                items: [
                ]
            },
            {
                title: "Community",
                items: [
                    { title: "Community Hub", path: "documents/community/index.md" },
                    { title: "Discussion Forums", path: "documents/community/forums.md" },
                    { title: "Chat & Support", path: "documents/community/chat.md" },
                    { title: "Events & Meetups", path: "documents/community/events.md" },
                    { title: "Community Guidelines", path: "documents/community/guidelines.md" },
                    { title: "Contributing", path: "documents/community/contributing.md" }
                ]
            },
            {
                title: "Resources",
                items: [
                    { title: "All Resources", path: "documents/resources/index.md" }
                ]
            },
            {
                title: "Downloads",
                items: [
                    { title: "Latest Release", path: "documents/downloads/index.md" }
                ]
            }
        ];
    }

    render(onItemClick) {
        const nav = document.getElementById('navigation');
        nav.innerHTML = '';

        this.navigation.forEach(section => {
            const sectionDiv = document.createElement('div');
            sectionDiv.className = 'nav-section';

            const sectionTitle = document.createElement('h3');
            sectionTitle.textContent = section.title;
            sectionDiv.appendChild(sectionTitle);

            const ul = document.createElement('ul');
            section.items.forEach(item => {
                const li = document.createElement('li');
                const a = document.createElement('a');
                a.href = `#${item.path}`;
                a.textContent = item.title;
                a.addEventListener('click', (e) => {
                    e.preventDefault();
                    onItemClick(item.path, a);
                });
                li.appendChild(a);
                ul.appendChild(li);
            });

            sectionDiv.appendChild(ul);
            nav.appendChild(sectionDiv);
        });
    }

    updateActiveItem(activeLink) {
        document.querySelectorAll('.nav a').forEach(link => {
            link.classList.remove('active');
        });

        if (activeLink) {
            activeLink.classList.add('active');
        }
    }

    findItemByPath(path) {
        for (const section of this.navigation) {
            for (const item of section.items) {
                if (item.path === path) {
                    return { item, section };
                }
            }
        }
        return null;
    }

    getFirstItem() {
        return this.navigation.length > 0 && this.navigation[0].items.length > 0 
            ? this.navigation[0].items[0] 
            : null;
    }
}

export default NavigationManager;