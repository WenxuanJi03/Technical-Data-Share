<template>
    <div :class="{'has-logo':showLogo}" :style="{ backgroundColor: settings.sideTheme === 'theme-dark' ? variables.menuBackground : variables.menuLightBackground }">
        <logo v-if="showLogo" :collapse="isCollapse" />
        <el-scrollbar :class="settings.sideTheme" wrap-class="scrollbar-wrapper">
            <el-menu
                :default-active="activeMenu"
                :collapse="isCollapse"
                :background-color="settings.sideTheme === 'theme-dark' ? variables.menuBackground : variables.menuLightBackground"
                :text-color="settings.sideTheme === 'theme-dark' ? variables.menuColor : variables.menuLightColor"
                :unique-opened="true"
                :active-text-color="settings.theme"
                :collapse-transition="false"
                mode="vertical"
            >
                <sidebar-item
                    v-for="(route, index) in sidebarRouters"
                    :key="route.path  + index"
                    :item="route"
                    :base-path="route.path"
                />
            </el-menu>
        </el-scrollbar>
    </div>
</template>

<script>
import { mapGetters, mapState } from "vuex"
import Logo from "./Logo"
import SidebarItem from "./SidebarItem"
import variables from "@/assets/styles/variables.scss"

export default {
    components: { SidebarItem, Logo },
    computed: {
        ...mapState(["settings"]),
        ...mapGetters(["sidebarRouters", "sidebar"]),
        activeMenu() {
            const route = this.$route
            const { meta, path } = route
            // if set path, the sidebar will highlight the path you set
            if (meta.activeMenu) {
                return meta.activeMenu
            }
            return path
        },
        showLogo() {
            return this.$store.state.settings.sidebarLogo
        },
        variables() {
            return variables
        },
        isCollapse() {
            return !this.sidebar.opened
        }
    },
    mounted() {
        // 延迟2秒检查，确保用户信息已加载
        setTimeout(() => {
            console.log('开始检查逾期任务...')
            this.$store.dispatch('overdue/checkUserOverdue').then(result => {
                console.log('逾期检查完成，结果:', result, '当前hasOverdue状态:', this.$store.getters.hasOverdue)
            })
        }, 2000)
        // 每5分钟检查一次
        this.overdueTimer = setInterval(() => {
            this.$store.dispatch('overdue/checkUserOverdue')
        }, 5 * 60 * 1000)
    },
    beforeDestroy() {
        if (this.overdueTimer) {
            clearInterval(this.overdueTimer)
        }
    }
}
</script>

<style lang="scss">
// 逾期任务菜单样式
.overdue-submenu > .el-submenu__title {
    color: #f56c6c !important;
    font-weight: bold;
    
    span {
        color: #f56c6c !important;
    }
    
    .svg-icon {
        color: #f56c6c !important;
    }
}

.overdue-menu-item {
    color: #f56c6c !important;
    font-weight: bold;
    
    span {
        color: #f56c6c !important;
    }
    
    .svg-icon {
        color: #f56c6c !important;
    }
}
</style>
