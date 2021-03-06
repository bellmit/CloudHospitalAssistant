import Vue from 'vue'
import Router from 'vue-router'
import Home from './views/Home.vue'
import Login from './views/Login'

Vue.use(Router)

export default new Router({
    routes: [
        {
            path: '/',
            name: 'home',
            component: Home
        },
        {
            path: '/admin/user',
            name: 'adminUser',
            // route level code-splitting
            // this generates a separate chunk (about.[hash].js) for this route
            // which is lazy-loaded when the route is visited.
            component: () => import('./views/admin/User')
        }, {
            path: '/admin/rule',
            name: 'schedule',
            component: ()=>import('./views/admin/Schedule')
        },
        {
            path: '/admin/other',
            name: 'other',
            // route level code-splitting
            // this generates a separate chunk (about.[hash].js) for this route
            // which is lazy-loaded when the route is visited.
            component: () => import('./views/admin/Other')
        },
        {
            path: '/doctor/index',
            name: 'doctor',
            component: () => import('./views/doctor/Index')
        }
        ,
        {
            path: '/doctor/statistics',
            name: 'doctorS',
            component: () => import('./views/doctor/Statistics')
        }, {
            path: '/login',
            name: 'login',
            component: Login
        },
        {
            path: '/patient/register',
            name: 'register',
            component: () => import('./views/patient/Register')
        },
        {
            path: '/patient/charge',
            name: 'charge',
            component: () => import('./views/patient/Charge')
        },
        {
            path: '/medicine/index',
            name: 'medicine',
            component: () => import( './views/medicine/Medicine')
        },
        {
            path: '/inspection/index',
            name: 'inspection',
            component: () => import('./views/inspection/Inspection')
        },
        {
            path: '/retreatRegister',
            name: 'RetreatRegister',
            component: () => import('./views/patient/RetreatRegister')
        },
        {
            path: '/search',
            name: 'search',
            component: () => import('./views/patient/Search')
        },
        {
            path: '/finance/manage',
            name: 'manage',
            component: () => import('./views/finance/PaymentType')
        },
        {
            path: '/finance/check',
            name: 'check',
            component: () => import('./views/finance/DailySettle')
        },
        {
            path: '/finance/workload',
            name: 'workload',
            component: () => import('./views/finance/WorkloadCalculate')
        },
        {
            path: '/patient/make',
            name: 'make',
            component: () => import('./views/patient/MakeDailySettle')
        },{
            path:'/constant',
            name:'constant',
            component:() => import('./views/admin/Constant')
        },
    ]
})
