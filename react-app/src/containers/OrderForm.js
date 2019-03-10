import React, { Component } from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import { withStyles } from '@material-ui/core/styles';
import Card from '@material-ui/core/Card';
import CardActions from '@material-ui/core/CardActions';
import CardContent from '@material-ui/core/CardContent';
import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';
import { connect } from 'react-redux';
import { saveOrder } from '../actions';

const styles = theme => ({
  container: {
    display: 'flex',
    flexWrap: 'wrap',
  },
  textField: {
    marginLeft: theme.spacing.unit,
    marginRight: theme.spacing.unit,
    width: 200,
  },
  dense: {
    marginTop: 19,
  },
  menu: {
    width: 200,
  },
});

class OrderForm extends Component {
  state = {
    name: 'Cat in the Hat',
    age: '',
    multiline: 'Controlled',
    currency: 'EUR',
  };

  handleChange = name => event => {
    this.setState({ [name]: event.target.value });
  };

  handleSubmit = (e) => {
    e.preventDefault();
    this.props.dispatch(saveOrder(this.state));
  }

  render() {

    const { classes } = this.props;

    return (
      <div>
        <form className={classes.container} noValidate autoComplete="off" onSubmit={this.handleSubmit}>
          <TextField
            id="_id"
            label="Order Id"
            className={classes.textField}
            value={this.state._id}
            onChange={this.handleChange('_id')}
            margin="normal"
          />
          <TextField
            id="threadId"
            label="Thread Id"
            className={classes.textField}
            value={this.state.threadId}
            onChange={this.handleChange('threadId')}
            margin="normal"
          />
          <TextField
            id="customerId"
            label="Customer Id"
            className={classes.textField}
            value={this.state.customerId}
            onChange={this.handleChange('customerId')}
            margin="normal"
          />
          <TextField
            id="state"
            label="Status"
            className={classes.textField}
            value={this.state.state}
            onChange={this.handleChange('state')}
            margin="normal"
          />
          <TextField
            id="created"
            label="Created Date"
            className={classes.dateField}
            value={this.state.created}
            onChange={this.handleChange('created')}
            margin="normal"
          />
          <TextField
            id="modified"
            label="Modified Date"
            className={classes.dateField}
            value={this.state.modified}
            onChange={this.handleChange('modified')}
            margin="normal"
          />
          <Button variant="contained" color="primary" className={classes.button} type="submit">
            Add Order
          </Button>
        </form>
      </div>
    )
  }
}

OrderForm.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default connect()(withStyles(styles)(OrderForm))