/*
 * File: /home/RinLin/Git/Ushio-Prober-Phigros/settings.gradle.kts
 * 
 * Copyright (c) 2026 RinLin-NYA a.k.a. Asahina Hotaru.
 * All Rights Reserved except the rights written below, and/or the
 * pre-written premission from the copyright holder(s).
 * 
 * This program is free software.
 * 
 * You can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * version 3.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 * 
 * SPDX-License-Identifier: GPL-3.0-only
 */

/*
 * File: /home/RinLin/Git/Ushio-Prober-Phigros/settings.gradle.kts
 * 
 * Copyright (c) 2026 RinLin-NYA a.k.a. Asahina Hotaru.
 * All Rights Reserved except the rights written below, and/or the
 * pre-written premission from the copyright holder(s).
 * 
 * This program is free software.
 * 
 * You can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * version 3.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 * 
 * SPDX-License-Identifier: GPL-3.0-only
 */

/*
 * File: /home/RinLin/Git/Ushio-Prober-Phigros/settings.gradle.kts
 * 
 * Copyright (c) 2026 RinLin-NYA a.k.a. Asahina Hotaru.
 * All Rights Reserved except the rights written below, and/or the
 * pre-written premission from the copyright holder(s).
 * 
 * This program is free software.
 * 
 * You can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * version 3.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 * 
 * SPDX-License-Identifier: GPL-3.0-only
 */

/*
 * File: /home/RinLin/Git/Ushio-Prober-Phigros/settings.gradle.kts
 * 
 * Copyright (c) 2026 RinLin-NYA a.k.a. Asahina Hotaru.
 * All Rights Reserved except the rights written below, and/or the
 * pre-written premission from the copyright holder(s).
 * 
 * This program is free software.
 * 
 * You can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * version 3.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 * 
 * SPDX-License-Identifier: GPL-3.0-only
 */

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Ushio-Prober-Phigros"
include(":app")
include(":domain")
include(":data")
