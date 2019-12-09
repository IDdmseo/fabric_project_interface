/*
Copyright IBM Corp., DTCC All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.example;

import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.ByteString;
import io.netty.handler.ssl.OpenSsl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import static java.nio.charset.StandardCharsets.UTF_8;
import com.owlike.genson.Genson;

public class CarTrade extends ChaincodeBase {
    
    private static Log _logger = LogFactory.getLog(CarTrade.class);
    private final Genson genson = new Genson();

    private static int registeredIdx = 0;
    private static int orderedIdx = 0;

    @Override
    public Response init(ChaincodeStub stub) {
        try {
            String func = stub.getFunction();
            if (!func.equals("init")) {
                return newErrorResponse("function other than init is not supported");
            }
            return newSuccessResponse();
        } catch (Throwable e) {
            return newErrorResponse(e);
        }
    }

    @Override
    public Response invoke(ChaincodeStub stub) {
        try {
            _logger.info("Invoke java chaincode");
            String func = stub.getFunction();
            List<String> params = stub.getParameters();
            if (func.equals("registerCar")) {
                return registerCar(stub, params);
            }
            if (func.equals("sellMyCar")) {
                return sellMyCar(stub, params);
            }
            if (func.equals("buyUserCar")) {
                return buyUserCar(stub, params);
            }
            if (func.equals("changeCarOwner")) {
                return changeCarOwner(stub, params);
            }
            if (func.equals("getMyCar")) {
                return getMyCar(stub, params);
            }
            if (func.equals("getAllRegisteredCar")) {
                return getAllRegisteredCar(stub, params);
            }
            if (func.equals("getAllOrderedCar")) {
                return getAllOrderedCar(stub, params);
            }
            return newErrorResponse("Invalid invoke function name.");
        } catch (Throwable e) {
            return newErrorResponse(e);
        }
    }

    private Response registerCar(ChaincodeStub stub, List<String> args) {
        String carword = "CAR";
        final String key = carword.concat(Integer.tostring(registeredIdx));

        String carState = stub.getStringState(key);
        if(!carState.isEmpty()){
            String errorMessage = String.format("Car %s already exists", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, FabCarErrors.CAR_ALREADY_EXISTS.toString());           
        }

        Car car = new Car(args.get(0),args.get(1),args.get(2),args.get(3));
        carState = genson.serialize(car);
        stub.putStringState(key, carState);

        return newSuccessResponse("invoke finished successfully");
    }

    private Response sellMyCar(ChaincodeStub stub, List<String> args) {
        String key = args.get(0);
        String carState = stub.getStringState(key);    
        Car currCar = genson.deserialize(carState, Car.class);
        String HashString = Integer.toString(currCar.hashCode());
        
        if(!(stub.getStringState(HashString)).IsEmpty()){
            String errorMessage = String.format("Car %s already exists in seller part", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, FabCarErrors.CAR_ALREADY_EXISTS.toString());           
        }

        stub.putStringState(HashString, "seller");
        
        return newSuccessResponse("invoke finished successfully");
    }

    private Response buyUserCar(ChaincodeStub stub, List<String> args) {
        String key = args.get(0);
        String carState = stub.getStringState(key);
        Car buyCar = genson.deserialize(carState, Car.class);
        String HashString;
        
        if(carState.IsEmpty()){
            String errorMessage = String.format("Car %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, FabCarErrors.CAR_NOT_FOUND.toString());
        }

        changeCarOwner(key, buyCar.getOwner());
        buyCar = genson.deserialize(carState, Car.class);
        HashString = Integer.toString(buyCar.HashCode());
        stub.putStringState(HashString, "done");
        
        return newSuccessResponse("invoke finished successfully");
    }

    private Response changeCarOwner(ChaincodeStub stub, List<String> args) {
        // params 0: key, 1: name;
        String carState = stub.getStringState(args.get(0));

        if(carState.isEmpty()){
            String errorMessage = String.format("Car %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, FabCarErrors.CAR_NOT_FOUND.toString());
        }

        Car car = genson.deserialize(carState, Car.class);
        Car newCar = Car(car.getMake(), car.getModel(), car.getColor(), args.get(1));
        carState = genson.serialize(newCar);
        stub.putStringState(carState);

        // return newCar;
        return newSuccessResponse("invoke finished successfully");
    }

    private Response getMyCar(ChaincodeStub stub, List<String> args) {
        // params 0: name;
        String startKey = "CAR".concat(Integer.toString(0));
        String endKey = "CAR".concat(Integer.toString(registeredIdx));
        String myName = args.get(0);
        List<Car> cars = new ArrayList<Car>();

        QueryResultsIterator<KeyValue> results = stub.getStateByRange(startKey, endKey);

        for(KeyValue result: results){
            Car car = genson.deserialize(result.getStringValue(), Car.class);
            if (myName == car.getOwner())
                cars.add(car);
        }

        Car[] response = cars.toArray(new Car[cars.size()]);
        //return newSuccessResponse(val, ByteString.copyFrom(val, UTF_8).toByteArray());  -> bytestring으로 val 값 return()
        return newSuccessResponse("invoke finished successfully");
    }

    private Response getAllRegisteredCar(ChaincodeStub stub, List<String> args) {
        String startKey = "CAR".concat(Integer.toString(0));
        String endKey = "CAR".concat(Integer.toString(registeredIdx));
        List<Car> cars = new ArrayList<Car>();
        //return newSuccessResponse(val, ByteString.copyFrom(val, UTF_8).toByteArray());  -> bytestring으로 val 값 return()

        QueryResultsIterator<KeyValue> results = stub.getStateByRange(startKey, endKey);

        for(KeyValue result: results){
            Car car = genson.deserialize(result.getStringValue(), Car.class);
            /* compare whether current car is being selled or not? */
            cars.add(car);
        }

        Car[] response = cars.toArray(new Car[cars.size()]);
        // return response ?
        return newSuccessResponse("invoke finished successfully");
    }

    private Response getAllOrderedCar(ChaincodeStub stub, List<String> args) {
        
        //return newSuccessResponse(val, ByteString.copyFrom(val, UTF_8).toByteArray());  -> bytestring으로 val 값 return()
        return newSuccessResponse("invoke finished successfully");
    }

    public static void main(String[] args) {
        System.out.println("OpenSSL avaliable: " + OpenSsl.isAvailable());
        new CarTrade().start(args);
    }
}
