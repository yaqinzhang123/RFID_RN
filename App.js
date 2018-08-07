/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, { Component } from 'react';
import { View, TouchableOpacity, Text, NativeModules, DeviceEventEmitter } from 'react-native';

const rnToastAndroid = NativeModules.RNToastAndroid;

class App extends Component {
  state = { obj: '' }

  componentWillMount() {
      DeviceEventEmitter.addListener('EventName', (res) => {
        //console.log(res);
        this.setState({ obj: res });
      });
  }
  // ontab(){
  // 	rnToastAndroid.read((str)=>{
  //     console.log(str);
  //     this.setState({obj:str});
  //    })
  // }
  


  render() {
  return (
    <View>
     <TouchableOpacity
      onPress={() => rnToastAndroid.readrfid()}
      style={styles.containerStyle}
      >
        <Text>读取RFID</Text>
      </TouchableOpacity>
      <TouchableOpacity
      onPress={() => rnToastAndroid.readrfid()}
      style={styles.containerStyle}
      >
        <Text>取消MAP</Text>
      </TouchableOpacity>
      <Text>{this.state.obj.RFID}</Text>
      
    </View>
  );
 }
}

const styles = {
 containerStyle: {
   height: 50,
   borderWidth: 1,
   borderColor: '#000',
   borderRadius: 5,
   padding: 3,
   margin: 5,
   justifyContent: 'center',
   alignItems: 'center'
 }
};

export default App;
