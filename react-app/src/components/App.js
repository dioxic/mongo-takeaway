import React from 'react';
import logo from '../logo.svg';
import './App.css';
import Footer from './Footer'
import OrderForm from '../containers/OrderForm';
import VisibleOrderList from '../containers/VisibleOrderList';

const App = () => (
  <div className="App">
    <header className="App-header">
      <img src={logo} className="App-logo" alt="logo" />
    </header>
    <OrderForm/>
    <VisibleOrderList />
    <Footer />
  </div>
)

export default App;
