/*
 * SPDX-License-Identifier: Apache-2.0
 */

'use strict';

const { FileSystemWallet, Gateway } = require('fabric-network');
const path = require('path');

const ccpPath = path.resolve(__dirname, '..', 'fabric-network', 'connection-org1.json');

var invoke = async function(name, fun, args){
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

        if (fun == registerCar){
            var make = document.getElementById("make").value;
            var model = document.getElementById("model").value;
            var color = document.getElementById("color").value;

            await contract.submitTransaction('registerCar', make, model, color, name);
        } 
        else if (fun == changeOwnerName){
            await contract.submitTransaction('changeOwnerName', name);
        }
        else if (fun == sellMyCar){
            // need to pick the car
            var key;
            await contract.submitTransaction('sellMyCar', keyId);
        }
        else if (fun == buyUserCar){
            // need to pick the car
            var key;
            await contract.submitTransaction('buyUserCar',key, name);
        } 

        //await contract.submitTransaction('createCar', 'CAR12', 'Honda', 'Accord', 'Black', 'Tom'); -> call invoke function in chaincode
        console.log('Transaction has been submitted');

        // Disconnect from the gateway.
        await gateway.disconnect();

    } catch (error) {
        console.error(`Failed to submit transaction: ${error}`);
        process.exit(1);
    }
}

exports.invoke = invoke;
