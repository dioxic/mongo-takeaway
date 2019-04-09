import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Paper from '@material-ui/core/Paper';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemText from '@material-ui/core/ListItemText';
import ListSubheader from '@material-ui/core/ListSubheader';
import ListItemAvatar from '@material-ui/core/ListItemAvatar';
import Avatar from '@material-ui/core/Avatar';
import IconButton from '@material-ui/core/IconButton';
import FolderIcon from '@material-ui/icons/Folder';
import Fab from '@material-ui/core/Fab';
import DeleteIcon from '@material-ui/icons/Delete';
import AddIcon from '@material-ui/icons/Add';
import DragHandleIcon from '@material-ui/icons/DragHandle';
import EditIcon from '@material-ui/icons/Edit';
import { SortableContainer, SortableElement, sortableHandle } from 'react-sortable-hoc';
import arrayMove from 'array-move';

const styles = theme => ({
  root: {
    width: '100%',
    //maxWidth: 360,
    backgroundColor: theme.palette.background.paper,
    position: 'relative',
    overflow: 'auto',
    maxHeight: 500,
  },
  paper: {
    marginTop: theme.spacing.unit * 3,
    marginBottom: theme.spacing.unit * 3,
    padding: theme.spacing.unit * 2,
    [theme.breakpoints.up(600 + theme.spacing.unit * 3 * 2)]: {
      marginTop: theme.spacing.unit * 6,
      marginBottom: theme.spacing.unit * 6,
      padding: theme.spacing.unit * 3,
    }
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

const SortableItem = SortableElement(({ value, classes, ...other }) => {
  if (value.section) {
    return (
      // <ListItem {...other} className={classes.listItemHeader}>
        <ListSubheader {...other} className={classes.listHeader}>{value.section}</ListSubheader>
      // </ListItem>
      )
  }
  else {
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
  }
});

const SortableListOld = SortableContainer(({ items, classes }) => {
  return (
    // <List subheader={<ListSubheader>Menu Items</ListSubheader>}>
    <List>
      {/* {["Appetisers", "Mains"].map(section => (
        <li key={`section-${section}`} className={classes.listSection}>
          <ul className={classes.ul}>
            <ListSubheader>{section}</ListSubheader> */}
      {items.map((value, index) => (
        <SortableItem classes={classes} key={`item-${index}`} index={index} value={value} />
      ))}
      {/* </ul>
        </li>
      ))} */}
    </List>
  );
});

const SortableList = SortableContainer(({ items, classes }) => {
  return (
    <List subheader={<ListSubheader>Menu Items</ListSubheader>}>
    {/* <List> */}
      {items.map((value, index) => (
        <SortableItem classes={classes} key={`item-${index}`} index={index} value={value} disabled={value.section} />
      ))}
    </List>
  );
});

function data2() {
  return [0, 1, 2, 3, 4, 5, 6].map(value => ({
    primary: `Item ${value}`,
    secondary: "secondary text",
    //img: "https://shortlist.imgix.net/app/uploads/2017/10/05141423/the-british-public-has-a-new-favourite-curry-crop-1507212989-1341x1341.jpg"
  })
  );
}

function data() {
  return [{
    section: "Appetisers",
    index: 1,
  }, {
    primary: "Item 1",
    secondary: "secondary text",
    index: 2
  }, {
    primary: "Item 2",
    secondary: "secondary text",
    index: 3
  }, {
    primary: "Item 3",
    secondary: "secondary text",
    index: 4
  }, {
    section: "Mains",
    index: 5
  }, {
    primary: "Item 6",
    secondary: "secondary text",
    index: 6
  }, {
    primary: "Item 7",
    secondary: "secondary text",
    index: 7
  }, {
    primary: "Item 8",
    secondary: "secondary text",
    index: 8
  }];
}

function dataOld() {
  return ["Appetisers", "Mains"].map((title, index) => ({
    title,
    index,
    items: [0, 1, 2, 3, 4, 5, 6].map(value => ({
      primary: `Item ${value}`,
      secondary: "secondary text",
      //img: "https://shortlist.imgix.net/app/uploads/2017/10/05141423/the-british-public-has-a-new-favourite-curry-crop-1507212989-1341x1341.jpg"
      index: `${index}`
    }))
  })
  );


  [0, 1, 2, 3, 4, 5, 6].map(value => ({
    primary: `Item ${value}`,
    secondary: "secondary text",
    //img: "https://shortlist.imgix.net/app/uploads/2017/10/05141423/the-british-public-has-a-new-favourite-curry-crop-1507212989-1341x1341.jpg"
  })
  );
}

function MenuList(props) {
  const [items, setItems] = useState(data())

  const onSortEnd = ({ oldIndex, newIndex }) => {
    setItems(arrayMove(items, oldIndex, newIndex));
  };

  return (
    <Paper className={props.classes.paper}>
      <SortableList classes={props.classes} items={items} onSortEnd={onSortEnd} lockAxis="y" useDragHandle />
      <div className={props.classes.fab}>
        <Fab color="primary" aria-label="Add">
          <AddIcon />
        </Fab>
      </div>
    </Paper>
  );
}

// function MenuList(props) {
//   const { classes } = props;

//   return (
//     <Paper className={classes.paper}>
//       <List className={classes.root} subheader={<li />}>
//         {[0, 1, 2, 3, 4].map(sectionId => (
//           <li key={`section-${sectionId}`} className={classes.listSection}>
//             <ul className={classes.ul}>
//               <ListSubheader>{`I'm sticky ${sectionId}`}</ListSubheader>
//               {[0, 1, 2].map(item => (
//                 <SortableItem key={`item-${sectionId}-${item}`} value={item} sectionId={sectionId} />
//                 // <ListItem key={`item-${sectionId}-${item}`}>
//                 //   <ListItemText primary={`Item ${item}`} />
//                 // </ListItem>
//               ))}
//             </ul>
//           </li>
//         ))}
//       </List>
//     </Paper>
//   );
// }

MenuList.propTypes = {
  classes: PropTypes.object.isRequired,
};


export default withStyles(styles)(MenuList);