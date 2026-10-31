# 📋 Tangible Fields Master Reference (`references/fields.md`)

Source: `D:\laragon\www\tangible-fields\wp-content\plugins\fields`

---

## 1. Accessing Fields in PHP

```php
require_once __DIR__ . '/vendor/tangible/fields/index.php';

$fields = tangible_fields();

// Render field in PHP
echo $fields->render_field( 'my_field_name', [
  'type'  => 'text',
  'label' => 'Field Label',
  'value' => $fields->fetch_value( 'my_field_name' ),
] );
```

---

## 2. Accessing Fields in JavaScript / React

> [!IMPORTANT]
> **React Admin Screens vs. Page Builder Tags:**
> - For standard WordPress admin screens, settings tabs, and custom React dashboards, **do NOT use `window.tangibleFields.render()`**. Use **`@tangible/ui`** (`<Field>`, `<TextInput>`, `<SecretInput>`, `<Button>`) instead (see [`references/ui.md`](./ui.md)).
> - `tangible/fields` (`vendor/tangible/fields`) is designed for PHP schema registration, CPT repeaters via `tangible/object`, and page-builder dynamic shortcode/tag insertion. Calling `tangibleFields.render()` in a custom React admin SPA wraps inputs in a borderless CodeMirror editor container (`.cm-editor`) intended for Beaver Builder/Elementor canvas environments.

When Tangible Fields is enqueued, `window.tangibleFields` is available globally. In `tangible.config.js`:
```javascript
export default {
  build: [
    {
      src: 'assets/src/index.jsx',
      dest: 'assets/build/index.min.js',
      react: 'wp',
      importToGlobal: {
        'tangible-fields': 'window.tangibleFields',
      },
    },
  ],
}
```

### Rendering via JavaScript
```javascript
import { createRoot } from 'react-dom'

const component = window.tangibleFields.render({
  type: 'text',
  name: 'field-name',
  label: 'Text Label',
  onChange: (newValue) => {
    console.log('Updated:', newValue)
  },
})

createRoot(document.getElementById('container-id')).render(component)
```

> **Note on Naming:** In PHP, arguments use `snake_case` (`enable_opacity`). In JavaScript, arguments use `camelCase` (`enableOpacity`).

---

## 3. All 25+ Built-in Field Types

| Field Type | Description & Key Options |
| :--- | :--- |
| `text` | Standard text input. Supports `placeholder`, `default`. |
| `textarea` | Multi-line text. Supports `rows`. |
| `number` | Numeric input. Supports `min`, `max`, `step`. |
| `select` | Dropdown. `choices => [ 'key' => 'Label' ]`. |
| `select3` | Enhanced select with multi-select and tags support. |
| `switch` | Boolean toggle. Returns `true` / `false`. |
| `checkbox` | Checkbox toggle. |
| `radio` | Radio list. `choices => [ 'key' => 'Label' ]`. |
| `button_group` | Segmented button choices. `choices => [ 'opt1' => 'One', 'opt2' => 'Two' ]`. |
| `color_picker` | Color picker with alpha support: `'enable_opacity' => true`. |
| `date_picker` | Calendar date picker. |
| `time_picker` | Clock time picker. |
| `file` | Media library single file selector. |
| `gallery` | Media library multi-image selector. |
| `accordion` | Collapsible section container for nested sub-fields. |
| `code` | Code editor (HTML/CSS/JS). Supports `language`. |
| `wysiwyg` | Full WordPress rich text / TinyMCE editor. |
| `dimensions` | 4-sided dimension control (Top, Right, Bottom, Left). |
| `simple_dimension` | Single dimension with unit selector (`px`, `rem`, `%`). |
| `alignment_matrix` | 9-point grid alignment selector (top-left to bottom-right). |
| `angle_picker` | Circular angle picker (0–360°). |
| `border` | Border style, width, and color group. |
| `gradient` | Linear/radial gradient builder. |
| `combo_box` | Autocomplete search dropdown. |
| `combo_box_async` | Remote AJAX/REST autocomplete search dropdown. |
| `hidden` | Hidden input for IDs, nonces, or JSON strings. |
| `list` | Reorderable, enable/disable key-value list with `_canDelete` and `_enabled` flags. |

---

## 4. Repeater Layouts & Formats

Repeaters allow adding multiple rows of sub-fields.

```php
<?= $fields->render_field( 'items', [
  'type'       => 'repeater',
  'label'      => 'Items',
  'layout'     => 'advanced', // 'advanced' | 'bare' | 'block' | 'tab' | 'table'
  'use_bulk'   => true,       // Optional bulk duplicate/remove actions
  'newItem'    => [           // Default values for freshly added rows
    'status' => 'active',
  ],
  'header_fields' => [        // Fields to show when row is collapsed
    'title',
    [ 'name' => 'status', 'callback' => 'formatStatusLabel' ],
  ],
  'sub_fields' => [
    [
      'name'  => 'title',
      'type'  => 'text',
      'label' => 'Item Title',
    ],
    [
      'name'    => 'status',
      'type'    => 'select',
      'label'   => 'Status',
      'choices' => [ 'active' => 'Active', 'paused' => 'Paused' ],
    ],
  ],
] ); ?>
```

### Registering JS Callbacks for Overview Header Fields
```javascript
if (window.tangibleFields?.fields?.repeater) {
  window.tangibleFields.fields.repeater.registerCallback(
    'formatStatusLabel',
    ({ value }) => value ? value.toUpperCase() : 'N/A'
  )
}
```

---

## 5. Conditional Logic

Fields can be shown/hidden based on other field values:

```php
[
  'name'      => 'secret_key',
  'type'      => 'text',
  'label'     => 'Secret Key',
  'condition' => [
    'action'    => 'show', // 'show' | 'hide'
    'condition' => [
      'auth_mode' => [ '_eq' => 'api_key' ], // '_eq', '_neq', '_in'
    ],
  ],
]
```
