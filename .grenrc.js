module.exports = {
  "dataSource": "prs",
  "prefix": "",
  "onlyMilestones": false,
  "groupBy": {
    "Enhancements": [
      "enhancement",
      "internal",
      "feat",
      "feature",
      "code improvement"
    ],
    "Bug Fixes": [
      "fix",
      "bug"
    ],
    "Documentation": [
      "doc",
      "documentation"
    ],
    "Others": [
      "other",
      "chore"
    ]
  },
  "ignoreIssuesWith": [
    "wontfix",
    "duplicate"
],
  "changelogFilename": "CHANGELOG.md",
  "template": {
      commit: ({ message, url, author, name }) => `- [${message}](${url}) - ${author ? `@${author}` : name}`,
      issue: "- {{name}} [{{text}}]({{url}})",
      noLabel: "other",
      group: "\n#### {{heading}}\n",
      changelogTitle: "# Changelog\n\n",
      release: "## {{release}} ({{date}})\n{{body}}",
      releaseSeparator: "\n---\n\n"
  }
}
