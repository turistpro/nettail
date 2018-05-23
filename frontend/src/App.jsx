import React from 'react';
import {connect } from 'react-redux';
import { push } from 'react-router-redux';


class App extends React.Component {

    constructor(props) {
        super(props);
        this.state = {}
        this.handleInputChange = this.handleInputChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }


    handleInputChange(event) {
        const target = event.target;
        const value = target.type === 'checkbox' ? target.checked : target.value;
        const name = target.name;
    
        this.setState({
          [name]: value
        });
    }

    handleSubmit(e) {
        e.preventDefault();
        const { host, user, path } = this.state;
        const url = "tail?path=ssh://" + user + "@" + host + path;
        this.props.push(url);
    }

    render() {
        return (
            <div>
                <h1>netTail App</h1>
                <form onSubmit={ this.handleSubmit }>
                    <div>host: <input type="text" name="host" placeholder="server-app" onChange={this.handleInputChange}/></div>
                    <div>user: <input type="text" name="user" placeholder="logview" onChange={this.handleInputChange}/></div>
                    <div>path: <input type="text" name="path" placeholder="/LOG/app.log" onChange={this.handleInputChange}/></div>
                    <button>tail</button>
                </form>
            </div>
        );
    }
}

const mapStateToProps = state => ({
});

const mapDispatchToProps = {
    push
}

export default connect(mapStateToProps, mapDispatchToProps)(App);