import React from 'react';


class Log extends React.Component {

    shouldComponentUpdate(nextProps, nextState) {
        return this.props.id != nextProps.id;
    }

    render() {
        return (
            <div id={ this.props.id }>
                { this.props.message }
            </div>
        )
    }
}

export default Log;