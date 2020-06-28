module.exports = {
  "dataSource": "prs",
  "prefix": "",
  "onlyMilestones": false,
  "groupBy": {
    "Enhancements": [
      "enhancement",
      "internal",
      "feat"
    ],
    "Bug Fixes": [
      "fix"
    ],
    "Documentation": [
      "doc"
    ],
    "Others": [
      "other"
    ]
  },
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
