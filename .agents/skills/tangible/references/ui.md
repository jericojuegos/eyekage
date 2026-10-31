# 🎨 `@tangible/ui` Component Master Reference (`references/ui.md`)

Source: `node_modules/@tangible/ui`  
Official Storybook Documentation: [storybook-tangible-ui.pages.dev](https://storybook-tangible-ui.pages.dev/?path=/docs/about--docs)  
Markdown Documentation (LLM-friendly): [storybook-tangible-ui.pages.dev/llms.txt](https://storybook-tangible-ui.pages.dev/llms.txt)

---

## 1. Overview & Separation of Concerns

`@tangible/ui` is Team Tangible's official accessible React design system library exporting **46 production-ready components** for admin dashboards, settings tabs, and management interfaces.

> [!IMPORTANT]
> **Form Controls Rule:**
> - When building custom React admin screens, **ALWAYS use `@tangible/ui`** (`Field`, `TextInput`, `SecretInput`, `Select`, `Button`). They provide native 1px token borders (`--tui-color-border-control`), focus rings, hover states, and accessibility (`aria-labelledby`, `aria-describedby`) out of the box with zero custom CSS.
> - **Never use `window.tangibleFields.render()` for basic admin form fields.** `tangible/fields` is designed for PHP schema registration, CPT repeaters via `tangible/object`, and page-builder dynamic shortcode/tag insertion.

---

## 2. Mandatory Wiring Setup

### A. CSS Stylesheet (Required Once App-Wide)
In SCSS (e.g., `assets/src/tui.scss` or `admin.scss`):
```scss
@import '../../node_modules/@tangible/ui/styles/all.unlayered.css';
```
Or in a modern JS/React entrypoint:
```javascript
import '@tangible/ui/styles';
```

### B. Root Container Class (`.tui-interface`)
All `@tangible/ui` design tokens are scoped under `.tui-interface`. **Every React root mount container MUST carry this class:**
```jsx
export default function App() {
  return (
    <div className="tui-interface">
      {/* All @tangible/ui components go inside here */}
    </div>
  )
}
```

---

## 3. Key Form Components & Code Examples

### A. Form Field Group (`Field`, `TextInput`, `SecretInput`)

```jsx
import { Field, TextInput, SecretInput, Button } from '@tangible/ui'

export default function ConnectionSettings({ settings, onSave }) {
  return (
    <div className="space-y-4">
      {/* Standard Text Input */}
      <Field required>
        <Field.Label>Destination Name</Field.Label>
        <Field.Control>
          <TextInput
            id="destination-name"
            value={settings.name || ''}
            onChange={(e) => onSave({ ...settings, name: e.target.value })}
            placeholder="e.g. Staging Site 1"
          />
        </Field.Control>
        <Field.HelperText>A friendly label for this remote connection.</Field.HelperText>
      </Field>

      {/* Secret Input (API Key / Password with built-in Reveal/Mask Eye Toggle) */}
      <Field required>
        <Field.Label>
          API Key
          <span className="tooltip-icon" title="36-character UUIDv4 remote authorization key"> ⓘ</span>
        </Field.Label>
        <Field.Control>
          <SecretInput
            id="destination-api-key"
            value={settings.apiKey || ''}
            onChange={(e) => onSave({ ...settings, apiKey: e.target.value })}
            placeholder="Paste remote API key"
          />
        </Field.Control>
      </Field>

      <Button label="Save Changes" theme="primary" onClick={() => onSave(settings)} />
    </div>
  )
}
```

### B. Selection Controls (`Select`, `Switch`, `Checkbox`)

```jsx
import { Field, Select, Switch, Checkbox } from '@tangible/ui'

// Select uses onValueChange for composite state
<Field>
  <Field.Label>Post Type Filter</Field.Label>
  <Field.Control>
    <Select
      value={selectedType}
      onValueChange={(val) => setSelectedType(val)}
      options={[
        { value: 'all', label: 'All Post Types' },
        { value: 'post', label: 'Posts' },
        { value: 'page', label: 'Pages' },
        { value: 'sfwd-courses', label: 'LearnDash Courses' },
      ]}
    />
  </Field.Control>
</Field>

// Switch & Checkbox use onCheckedChange
<Switch
  label="Enable Automatic Syncs"
  checked={isEnabled}
  onCheckedChange={(checked) => setIsEnabled(checked)}
/>
```

### C. Notice Banners (`Notice`)

```jsx
import { Notice } from '@tangible/ui'

{saveSuccess && (
  <Notice
    theme="success"
    title="Connection Verified"
    onDismiss={() => setSaveSuccess(false)}
  >
    Remote site successfully connected with 200 OK ping.
  </Notice>
)}

{saveError && (
  <Notice
    theme="danger"
    title="Connection Failed"
  >
    {saveError.message || 'Remote endpoint unreachable.'}
  </Notice>
)}
```

### D. Modals (`Modal`)

```jsx
import { Modal, Button } from '@tangible/ui'

<Modal
  open={isModalOpen}
  onOpenChange={setIsModalOpen}
  title="Add Remote Destination"
  description="Configure endpoint credentials for pushing content."
>
  <div className="p-4 space-y-4">
    {/* Form fields */}
  </div>
  <Modal.Footer>
    <Button label="Cancel" variant="outline" onClick={() => setIsModalOpen(false)} />
    <Button label="Save Destination" theme="primary" onClick={handleSave} />
  </Modal.Footer>
</Modal>
```

---

## 4. Icons in `@tangible/ui`

Icons can be imported directly as React components from `@tangible/ui/icons/system`:

```jsx
import { IconButton } from '@tangible/ui'
import { Plus, Trash, Copy, Check } from '@tangible/ui/icons/system'

// Pass component directly to icon prop (bypasses runtime string registry)
<IconButton
  icon={Copy}
  label="Copy API Key"
  onClick={handleCopy}
/>

<IconButton
  icon={Trash}
  label="Delete Connection"
  theme="danger"
  variant="ghost"
  onClick={handleDelete}
/>
```

---

## 5. Summary of Component Conventions

| Concern | Convention |
| :--- | :--- |
| **Composite Value Change** | `onValueChange` (`Select`, `Combobox`, `MultiSelect`) |
| **Native Input Change** | `onChange` (`TextInput`, `Textarea`, `SecretInput`) |
| **Boolean Toggles** | `onCheckedChange` (`Checkbox`, `Switch`) |
| **Floating Overlays** | `open`, `defaultOpen`, `onOpenChange` (`Modal`, `Popover`, `Dropdown`) |
| **Visual Intent Theme** | `theme="primary" \| "secondary" \| "success" \| "warning" \| "danger" \| "info"` |
| **Visual Variant** | `variant="solid" \| "outline" \| "ghost" \| "link"` |
| **Clear Values** | Clearable controls return `undefined` on clear (not `""`) |
