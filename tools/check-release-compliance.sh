#!/usr/bin/env bash
set -euo pipefail

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$project_dir"

version_name="$(sed -n 's/^[[:space:]]*versionName "\([^"]*\)"/\1/p' build.gradle | head -n 1)"
version_code="$(sed -n 's/^[[:space:]]*versionCode \([0-9][0-9]*\)/\1/p' build.gradle | head -n 1)"
fastlane_code="${version_code}04"
errors=0

require_file() {
    if [[ ! -s "$1" ]]; then
        printf 'ERREUR: fichier absent ou vide: %s\n' "$1" >&2
        errors=$((errors + 1))
    fi
}

require_text() {
    if ! rg --quiet --fixed-strings "$2" "$1"; then
        printf 'ERREUR: %s ne contient pas %s\n' "$1" "$2" >&2
        errors=$((errors + 1))
    fi
}

require_file LICENSE
require_file NOTICE.md
require_file THIRD_PARTY_NOTICES.md
require_file TRADEMARKS.md
require_file "fastlane/metadata/android/fr-FR/changelogs/${fastlane_code}.txt"
require_file "fastlane/metadata/android/en-US/changelogs/${fastlane_code}.txt"
require_file src/conversations/res/raw/gpl_v3.txt
require_file src/conversations/res/raw/lgpl_v2_1.txt
require_file src/conversations/res/raw/apache_2_0.txt
require_file src/conversations/res/raw/permissive_license_notices.txt
require_file src/conversations/res/raw/third_party_notices.txt
require_file src/conversations/res/raw/release_notes.txt
require_file docs/maer-chat/RELEASE_COMPLIANCE.md

require_text README.md "Version Maer Chat : \`${version_name}\`"
require_text NOTICE.md "Maer Chat ${version_name}"
require_text THIRD_PARTY_NOTICES.md "Maer Chat ${version_name}"
require_text CHANGELOG.md "Version ${version_name}"
require_text src/conversations/res/raw/release_notes.txt "VERSION ${version_name}"

if [[ "${GITHUB_REF_TYPE:-}" == "tag" ]]; then
    expected_tag="v${version_name}"
    if [[ "${GITHUB_REF_NAME:-}" != "$expected_tag" ]]; then
        printf 'ERREUR: le tag %s ne correspond pas à la version %s\n' \
            "${GITHUB_REF_NAME:-inconnu}" "$expected_tag" >&2
        errors=$((errors + 1))
    fi
fi

if rg --quiet 'téléphone professionnel : \*\*à compléter|public B2B ou B2C.*à compléter|durées de conservation.*à compléter' \
    docs/maer-chat/CGU.md docs/maer-chat/PRIVACY.md docs/maer-chat/RELEASE_COMPLIANCE.md; then
    printf 'BLOCAGE JURIDIQUE: des informations obligatoires restent à compléter.\n' >&2
    errors=$((errors + 1))
fi

if rg --quiet 'L’auteur du logo|droits de redistribution' TRADEMARKS.md; then
    printf 'BLOCAGE LOGO: la titularité et la licence du visuel Maer Chat restent à documenter.\n' >&2
    errors=$((errors + 1))
fi

if ((errors > 0)); then
    printf 'Contrôle de publication échoué avec %d anomalie(s).\n' "$errors" >&2
    exit 1
fi

printf 'Contrôle de publication réussi pour Maer Chat %s (notes %s).\n' \
    "$version_name" "$fastlane_code"
