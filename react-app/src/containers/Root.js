import React, { Component } from 'react'
import { Provider } from 'react-redux'
import configureStore from '../store/configureStore'
import App from '../components/App'
import { VisibilityFilters } from '../redux/order'
import { func } from 'prop-types';

const store = configureStore({
    orders: {
        post: {
            data: {},
            fetching: false
        },
        many: {
            data: [],
            filter: VisibilityFilters.SHOW_ALL,
            fetching: false
        },
        one: {
            data: {},
            fetching: false
        }
    }
})

function Root() {
    return (
        <Provider store={store}>
            <App />
        </Provider>
    )
}

export default Root;