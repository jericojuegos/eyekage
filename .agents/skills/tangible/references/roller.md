# 🛠️ Tangible Roller Master Reference (`references/roller.md`)

Source: `@tangible/roller` & `tangible.config.js`

---

## 1. Canonical `tangible.config.js`

```javascript
export default {
  build: [
    // Frontend JS/JSX Bundle
    {
      src: 'assets/src/index.jsx',
      dest: 'assets/build/my-plugin.min.js',
      react: 'wp', // Maps React to window.wp.element
      importToGlobal: {
        'tangible-fields': 'window.tangibleFields',
      },
    },
    // Frontend SCSS Bundle
    {
      src: 'assets/src/index.scss',
      dest: 'assets/build/my-plugin.min.css',
    },
    // Admin SCSS Bundle
    {
      src: 'assets/src/admin.scss',
      dest: 'assets/build/admin.min.css',
    },
  ],

  format: [
    'assets/src',
    'includes',
    '*.{php,js,json}',
  ],

  archive: {
    root: 'my-plugin',
    src: [
      '*.php',
      'assets/**',
      'includes/**',
      'vendor/tangible/**',
      'readme.txt',
    ],
    dest: 'publish/my-plugin.zip',
    exclude: [
      'assets/src',
      'docs',
      'vendor/tangible/*/vendor',
      'vendor/tangible-dev/',
      '.git',
      '**/artifacts',
      '**/publish',
      '**/node_modules',
      '**/tests',
      '**/*.scss',
      '**/*.jsx',
      '**/*.ts',
      '**/*.tsx',
    ],
  },

  // Production Git Dependencies (Installed to vendor/tangible/*)
  install: [
    {
      git: 'git@github.com:tangibleinc/framework',
      dest: 'vendor/tangible/framework',
      branch: 'main',
    },
    {
      git: 'git@github.com:tangibleinc/fields',
      dest: 'vendor/tangible/fields',
      branch: 'main',
    },
    {
      git: 'git@github.com:tangibleinc/updater',
      dest: 'vendor/tangible/updater',
      branch: 'main',
    },
  ],
}
```

---

## 2. Command Reference

| Command | Action |
| :--- | :--- |
| `npm run dev` | Watches `assets/src/` and automatically compiles minified bundles to `assets/build/`. |
| `npm run build` | Compiles production minified JS/SCSS bundles. |
| `npm run format` | Runs Prettier on JS/JSON and PHP Beautify on PHP files. |
| `npx roll install` | Clones or updates Git dependencies in `vendor/tangible/*` based on `tangible.config.js`. |
| `npm run archive` | Packages a clean, production-ready `.zip` in `publish/` excluding source files, tests, and git metadata. |
