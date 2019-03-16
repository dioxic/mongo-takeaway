import React from 'react';
import logo from '../logo.svg';
import './App.css';
import Footer from './Footer'
import OrderForm from '../containers/OrderForm';
import VisibleOrderList from '../containers/VisibleOrderList';
import PropTypes from 'prop-types';
import Grid from '@material-ui/core/Grid';
import { withStyles } from '@material-ui/core/styles';
import Paper from '@material-ui/core/Paper';

const styles = theme => ({
  root: {
    flexGrow: 1,
  },
  paper: {
    height: 140,
    width: 100,
  },
  control: {
    padding: theme.spacing.unit * 2,
  },
});

class App extends React.Component {
  state = {
    spacing: '16',
  };

  render() {
    const { classes } = this.props;
    const { spacing } = this.state;
    return (
      <div className="App">
        <Grid container className={classes.root} spacing={16}>
          <Grid item xs={12}>
            <header className="App-header">
              <img src={logo} className="App-logo" alt="logo" />
            </header>
          </Grid>
          <Grid container item xs={5} justify="center">
            <OrderForm/>
          </Grid>
          <Grid item xs={12}>
            <VisibleOrderList />
          </Grid>
          <Grid item xs={12}>
            <Footer />
          </Grid>
        </Grid>
      </div>
      
    );
  }
}

App.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(App);
