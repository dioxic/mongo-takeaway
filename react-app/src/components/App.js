import React from 'react';
import logo from '../logo.svg';
import './App.css';
import Footer from './Footer'
import OrderForm from '../containers/OrderForm';
import VisibleOrderList from '../containers/VisibleOrderList';
import PropTypes from 'prop-types';
import Grid from '@material-ui/core/Grid';
import { withStyles } from '@material-ui/core/styles';
import Checkout from '../checkout';

const styles = () => ({
  root: {
    flexGrow: 1,
  }
});

function App({ classes }) {

    return (
      <div className="App">
        <Grid container className={classes.root} spacing={16}>
          <Grid item xs={12}>
            <header className="App-header">
              <img src={logo} className="App-logo" alt="logo" />
            </header>
          </Grid>
          <Grid item xs={6}>
            <OrderForm/>
          </Grid>
          <Grid item xs={6}>
            <Checkout/>
          </Grid>          
          <Grid item xs={6}>
            <VisibleOrderList />
            <Footer />
          </Grid>
        </Grid>
      </div>
      
    );

}

App.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(App);
