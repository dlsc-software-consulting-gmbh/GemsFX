### RecentFiles

Manages a "Recent Files" list, persisting file paths via Java `Preferences`
and providing a self-updating JavaFX `Menu` that reflects the current list.

Typical usage:
```
`RecentFiles recentFiles = new RecentFiles(
      Preferences.userNodeForPackage(MyApp.class));
  recentFiles.setOnOpenFile(file -> loadDocument(file));
  menuBar.getMenus().add(recentFiles.getMenu());

  // After the user opens a file:
  recentFiles.add(chosenFile);
`
```

The default maximum number of entries is {@value #DEFAULT_MAX_FILES}.
Use `setMaxFiles(int)` to change it.
