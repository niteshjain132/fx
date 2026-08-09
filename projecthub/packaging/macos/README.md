# macOS packaging assets for ProjectHub
#
# ProjectHub.png  — source icon (1024×1024). On a Mac, package-macos.sh
#                   converts this to ProjectHub.icns via sips + iconutil.
#
# Build the double-clickable app (must run on macOS / MacBook):
#   ./projecthub/scripts/package-macos.sh
#
# Outputs:
#   projecthub/target/dist/ProjectHub.app
#   projecthub/target/dist/ProjectHub-1.0.0.dmg
