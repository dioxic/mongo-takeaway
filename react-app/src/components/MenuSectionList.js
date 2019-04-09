import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import Typography from '@material-ui/core/Typography';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import { List, ListItem, ListItemText, ListSubheader, ListItemAvatar } from '@material-ui/core';
import Avatar from '@material-ui/core/Avatar';
import IconButton from '@material-ui/core/IconButton';
import DeleteIcon from '@material-ui/icons/Delete';
import AddIcon from '@material-ui/icons/Add';
import DragHandleIcon from '@material-ui/icons/DragHandle';
import EditIcon from '@material-ui/icons/Edit';
import { sortableContainer, sortableElement, sortableHandle } from 'react-sortable-hoc';
import arrayMove from 'array-move';

const styles = theme => ({
  root: {
    width: '100%',
  },
  heading: {
    fontSize: theme.typography.pxToRem(15),
    flexBasis: '33.33%',
    flexShrink: 0,
  },
  secondaryHeading: {
    fontSize: theme.typography.pxToRem(15),
    color: theme.palette.text.secondary,
  },
  listItem: {
    borderBottom: "1px solid #efefef",
    backgroundColor: theme.palette.background.paper,
  },
  listItemHeader: {
    borderBottom: "1px solid #efefef",
    backgroundColor: theme.palette.divider,
  },
  listHeader: {
    backgroundColor: theme.palette.divider,
  },
  panelDetails: {
    width: '100%',
    padding: 0,
  },
  dragHandle: {
    cursor: 'pointer'
  },
  listSection: {
    backgroundColor: 'inherit',
  },
  ul: {
    backgroundColor: 'inherit',
    padding: 0,
  },
  fab: {
    position: 'relative',
    display: 'flex',
    justifyContent: 'flex-end',
    marginTop: theme.spacing.unit * 2,
    marginLeft: theme.spacing.unit,
    // align: 'right',
    // bottom: theme.spacing.unit * 2,
    // right: theme.spacing.unit * 2,
  },
});

const DragHandle = sortableHandle(({ classes }) => <DragHandleIcon className={classes.dragHandle} />);

const SortableItem = sortableElement(({ value, classes, ...other }) => {
  return (
    <ListItem {...other} className={classes.listItem}>
      <DragHandle classes={classes} />
      {value.img &&
        <ListItemAvatar>
          <Avatar src={value.img} />
        </ListItemAvatar>
      }
      <ListItemText primary={value.primary} secondary={value.secondary} />
      <IconButton>
        <EditIcon />
      </IconButton>
      <IconButton color="secondary">
        <DeleteIcon />
      </IconButton>
    </ListItem>
  )
});

const SortableContainer = sortableContainer(({ children, classes }) => {
  return (
    <div>
      {children}
    </div>
  );
});

function dataOld() {
  return [{
    primary: "Item 1",
    secondary: "secondary text",
    index: 0
  }, {
    primary: "Item 2",
    secondary: "secondary text",
    index: 1
  }, {
    primary: "Item 3",
    secondary: "secondary text",
    index: 2
  }, {
    primary: "Item 6",
    secondary: "secondary text",
    index: 3
  }, {
    primary: "Item 7",
    secondary: "secondary text",
    index: 4
  }, {
    primary: "Item 8",
    secondary: "secondary text",
    index: 5
  }];
}

function data() {
  return [{
    title: "Appetisers",
    items: [
      {
        primary: "Item 1",
        secondary: "secondary text",
        index: 0
      }, {
        primary: "Item 2",
        secondary: "secondary text",
        index: 1
      }, {
        primary: "Item 3",
        secondary: "secondary text",
        index: 2
      }, {
        primary: "Item 6",
        secondary: "secondary text",
        index: 3
      }, {
        primary: "Item 7",
        secondary: "secondary text",
        index: 4
      }, {
        primary: "Item 8",
        secondary: "secondary text",
        index: 5
      }
    ]
  }, {
    title: "Mains",
    items: [
      {
        primary: "Item 1",
        secondary: "secondary text",
        index: 0
      }, {
        primary: "Item 2",
        secondary: "secondary text",
        index: 1
      }, {
        primary: "Item 3",
        secondary: "secondary text",
        index: 2
      }, {
        primary: "Item 6",
        secondary: "secondary text",
        index: 3
      }, {
        primary: "Item 7",
        secondary: "secondary text",
        index: 4
      }, {
        primary: "Item 8",
        secondary: "secondary text",
        index: 5
      }
    ]
  }];
}

function ControlledExpansionPanels(props) {
  const { classes } = props;
  const [expanded, setExpanded] = useState(null);
  const [sections, setSections] = useState(data());

  const handleChange = panel => (event, isExpanded) => {
    setExpanded(isExpanded ? panel : false);
  };

  const onSortEnd = ({ oldIndex, newIndex, collection }) => {
    const newSections = [...sections];
    newSections[collection].items = arrayMove(sections[collection].items, oldIndex, newIndex);
    setSections(newSections);
  };


  return (
    <div className={classes.root}>
      <SortableContainer classes={classes} onSortEnd={onSortEnd} lockAxis="y" useDragHandle>
        {sections.map((section, index) => (
          <ExpansionPanel key={`section-${index}`} expanded={expanded === section.title} onChange={handleChange(section.title)}>
            <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
              <Typography className={classes.heading}>{section.title}</Typography>
              <Typography className={classes.secondaryHeading}>I am an expansion panel</Typography>
            </ExpansionPanelSummary>
            <ExpansionPanelDetails className={classes.panelDetails}>
              <List className={classes.panelDetails}>
                {section.items.map((item, i) => (
                  <SortableItem
                    key={`section-${index}-item-${i}`}
                    classes={classes}
                    value={item}
                    index={i}
                    collection={index} />
                ))}
              </List>
              {/* <SortableList classes={classes} items={section.items} onSortEnd={onSortEnd} lockAxis="y" useDragHandle /> */}
            </ExpansionPanelDetails>
          </ExpansionPanel>
        ))}
      </SortableContainer>

      {/* <ExpansionPanel expanded={expanded === 'panel1'} onChange={handleChange('panel1')}>
        <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
          <Typography className={classes.heading}>General settings</Typography>
          <Typography className={classes.secondaryHeading}>I am an expansion panel</Typography>
        </ExpansionPanelSummary>
        <ExpansionPanelDetails className={classes.panelDetails}>
          <SortableList classes={classes} items={items} onSortEnd={onSortEnd} lockAxis="y" useDragHandle />
        </ExpansionPanelDetails>
      </ExpansionPanel>
      <ExpansionPanel expanded={expanded === 'panel2'} onChange={handleChange('panel2')}>
        <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
          <Typography className={classes.heading}>Users</Typography>
          <Typography className={classes.secondaryHeading}>
            You are currently not an owner
			</Typography>
        </ExpansionPanelSummary>
        <ExpansionPanelDetails>
          <Typography>
            Donec placerat, lectus sed mattis semper, neque lectus feugiat lectus, varius pulvinar
						diam eros in elit. Pellentesque convallis laoreet laoreet.
			</Typography>
        </ExpansionPanelDetails>
      </ExpansionPanel>
      <ExpansionPanel expanded={expanded === 'panel3'} onChange={handleChange('panel3')}>
        <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
          <Typography className={classes.heading}>Advanced settings</Typography>
          <Typography className={classes.secondaryHeading}>
            Filtering has been entirely disabled for whole web server
			</Typography>
        </ExpansionPanelSummary>
        <ExpansionPanelDetails>
          <Typography>
            Nunc vitae orci ultricies, auctor nunc in, volutpat nisl. Integer sit amet egestas eros,
						vitae egestas augue. Duis vel est augue.
			</Typography>
        </ExpansionPanelDetails>
      </ExpansionPanel>
      <ExpansionPanel expanded={expanded === 'panel4'} onChange={handleChange('panel4')}>
        <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
          <Typography className={classes.heading}>Personal data</Typography>
        </ExpansionPanelSummary>
        <ExpansionPanelDetails>
          <Typography>
            Nunc vitae orci ultricies, auctor nunc in, volutpat nisl. Integer sit amet egestas eros,
						vitae egestas augue. Duis vel est augue.
			</Typography>
        </ExpansionPanelDetails>
      </ExpansionPanel> */}
    </div>
  );
}

ControlledExpansionPanels.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(ControlledExpansionPanels);