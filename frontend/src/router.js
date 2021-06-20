
import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router);


import MemberManager from "./components/MemberManager"

import BookManager from "./components/BookManager"

import BookRequestManager from "./components/BookRequestManager"

import BookRentalManager from "./components/BookRentalManager"


import ViewBookInfo from "./components/viewBookInfo"
import ViewBookRental from "./components/viewBookRental"
import ViewMembers from "./components/viewMembers"
import WarningLetterManager from "./components/WarningLetterManager"

import ReasonLetterManager from "./components/reasonLetterManager"

export default new Router({
    // mode: 'history',
    base: process.env.BASE_URL,
    routes: [
            {
                path: '/Member',
                name: 'MemberManager',
                component: MemberManager
            },

            {
                path: '/Book',
                name: 'BookManager',
                component: BookManager
            },

            {
                path: '/BookRequest',
                name: 'BookRequestManager',
                component: BookRequestManager
            },

            {
                path: '/BookRental',
                name: 'BookRentalManager',
                component: BookRentalManager
            },


            {
                path: '/viewBookInfo',
                name: 'viewBookInfo',
                component: viewBookInfo
            },
            {
                path: '/viewBookRental',
                name: 'viewBookRental',
                component: viewBookRental
            },
            {
                path: '/viewMembers',
                name: 'viewMembers',
                component: viewMembers
            },
            {
                path: '/WarningLetter',
                name: 'WarningLetterManager',
                component: WarningLetterManager
            },

            {
                path: '/reasonLetter',
                name: 'reasonLetterManager',
                component: reasonLetterManager
            },



    ]
})
