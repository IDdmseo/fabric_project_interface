/*
 * SPDX-License-Identifier: Apache-2.0
 */

'use strict';

const { FileSystemWallet, Gateway } = require('fabric-network');
const path = require('path');

const ccpPath = path.resolve(__dirname, '..', 'fabric-network', 'connection-org1.json');

var query = async function(name){
    try {

        // Create a new file system based wallet for managing identities.
        const walletPath = path.join(process.cwd(), 'wallet');
        const wallet = new FileSystemWallet(walletPath);
        console.log(`Wallet path: ${walletPath}`);

        // Check to see if we've already enrolled the user.
        const userExists = await wallet.exists(name);
        if (!userExists) {
            console.log('An identity for the user does not exist in the wallet');
            console.log('Run the registerUser.js application before retrying');
            return;
        }

        // Create a new gateway for connecting to our peer node.
        const gateway = new Gateway();
        await gateway.connect(ccpPath, { wallet, identity: name, discovery: { enabled: true, asLocalhost: true } });

        // Get the network (channel) our contract is deployed to.
        const network = await gateway.getNetwork('mychannel');

        // Get the contract from the network.
        const contract = network.getContract('mycc');

        //execute query function
        //const result = await contract.evaluateTransaction('query', 'a');

        var mycar = await contract.evaluateTransaction('getMyCar', name);
        var showallRegCar = await contract.evaluateTransaction('getAllRegisteredCar');
        var showallSellCar = await contract.evaluateTransaction('getAllOrderedCar');

        //query result value(string), string to json and use 
        //var test = JSON.parse(result);  -> string to json
        //console.log('test: ', test['make']);  -> use json object
        console.log(`Transaction has been evaluated, result is: ${result.toString()}`);

        return result.toString();
        

    } catch (error) {
        console.error(`Failed to evaluate transaction: ${error}`);
        process.exit(1);
    }
}

exports.query = query;