import React from 'react';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import TextField from '@material-ui/core/TextField';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Checkbox from '@material-ui/core/Checkbox';

function PaymentForm({
    values,
    errors,
    touched,
    handleChange,
    handleBlur
}) {
    return (
        <React.Fragment>
            <Typography variant="h6" gutterBottom>
                Payment method
		</Typography>
            <Grid container spacing={24}>
                <Grid item xs={12} md={6}>
                    <TextField
                        required
                        id="cardName"
                        label="Name on card"
                        name="cardName"
                        fullWidth
                        helperText={touched.cardName ? errors.cardName : ""}
                        error={touched.cardName && Boolean(errors.cardName)}
                        onChange={handleChange}
                        onBlur={handleBlur}
                        value={values.cardName}
                    />
                </Grid>
                <Grid item xs={12} md={6}>
                    <TextField
                        required
                        id="cardNumber"
                        label="Card number"
                        name="cardNumber"
                        type="number"
                        fullWidth
                        helperText={touched.cardNumber ? errors.cardNumber : ""}
                        error={touched.cardNumber && Boolean(errors.cardNumber)}
                        onChange={handleChange}
                        onBlur={handleBlur}
                        value={values.cardNumber}
                    />
                </Grid>
                <Grid item xs={12} md={6}>
                    <TextField
                        required
                        id="expDate"
                        label="Expiry date"
                        name="expDate"
                        fullWidth
                        helperText={touched.expDate ? errors.expDate : ""}
                        error={touched.expDate && Boolean(errors.expDate)}
                        onChange={handleChange}
                        onBlur={handleBlur}
                        value={values.expDate}
                    />
                </Grid>
                <Grid item xs={12} md={6}>
                    <TextField
                        required
                        id="cvv"
                        label="CVV"
                        name="ccv"
                        type="number"
                        fullWidth
                        helperText={touched.ccv ? errors.ccv : "Last three digits on signature strip"}
                        error={touched.ccv && Boolean(errors.ccv)}
                        onChange={handleChange}
                        onBlur={handleBlur}
                        value={values.ccv}
                    />
                </Grid>
                <Grid item xs={12}>
                    <FormControlLabel
                        control={<Checkbox color="secondary" name="saveCard" value="yes" />}
                        label="Remember credit card details for next time"
                    />
                </Grid>
            </Grid>
        </React.Fragment>
    )
}

export default PaymentForm;