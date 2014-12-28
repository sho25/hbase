begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutorService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HConstants
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|TableName
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ipc
operator|.
name|RpcControllerFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Bytes
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|ExceptionUtil
import|;
end_import

begin_comment
comment|/**  * A reversed client scanner which support backward scanning  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ReversedClientScanner
extends|extends
name|ClientScanner
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|ReversedClientScanner
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// A byte array in which all elements are the max byte, and it is used to
comment|// construct closest front row
specifier|static
name|byte
index|[]
name|MAX_BYTE_ARRAY
init|=
name|Bytes
operator|.
name|createMaxByteArray
argument_list|(
literal|9
argument_list|)
decl_stmt|;
comment|/**    * Create a new ReversibleClientScanner for the specified table Note that the    * passed {@link Scan}'s start row maybe changed.    * @param conf    * @param scan    * @param tableName    * @param connection    * @param pool    * @param primaryOperationTimeout    * @throws IOException    */
specifier|public
name|ReversedClientScanner
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Scan
name|scan
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|ClusterConnection
name|connection
parameter_list|,
name|RpcRetryingCallerFactory
name|rpcFactory
parameter_list|,
name|RpcControllerFactory
name|controllerFactory
parameter_list|,
name|ExecutorService
name|pool
parameter_list|,
name|int
name|primaryOperationTimeout
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|scan
argument_list|,
name|tableName
argument_list|,
name|connection
argument_list|,
name|rpcFactory
argument_list|,
name|controllerFactory
argument_list|,
name|pool
argument_list|,
name|primaryOperationTimeout
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|nextScanner
parameter_list|(
name|int
name|nbRows
parameter_list|,
specifier|final
name|boolean
name|done
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Close the previous scanner if it's open
if|if
condition|(
name|this
operator|.
name|callable
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|callable
operator|.
name|setClose
argument_list|()
expr_stmt|;
comment|// callWithoutRetries is at this layer. Within the ScannerCallableWithReplicas,
comment|// we do a callWithRetries
name|this
operator|.
name|caller
operator|.
name|callWithoutRetries
argument_list|(
name|callable
argument_list|,
name|scannerTimeout
argument_list|)
expr_stmt|;
name|this
operator|.
name|callable
operator|=
literal|null
expr_stmt|;
block|}
comment|// Where to start the next scanner
name|byte
index|[]
name|localStartKey
decl_stmt|;
name|boolean
name|locateTheClosestFrontRow
init|=
literal|true
decl_stmt|;
comment|// if we're at start of table, close and return false to stop iterating
if|if
condition|(
name|this
operator|.
name|currentRegion
operator|!=
literal|null
condition|)
block|{
name|byte
index|[]
name|startKey
init|=
name|this
operator|.
name|currentRegion
operator|.
name|getStartKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|startKey
operator|==
literal|null
operator|||
name|Bytes
operator|.
name|equals
argument_list|(
name|startKey
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
operator|||
name|checkScanStopRow
argument_list|(
name|startKey
argument_list|)
operator|||
name|done
condition|)
block|{
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Finished "
operator|+
name|this
operator|.
name|currentRegion
argument_list|)
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
name|localStartKey
operator|=
name|startKey
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Finished "
operator|+
name|this
operator|.
name|currentRegion
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|localStartKey
operator|=
name|this
operator|.
name|scan
operator|.
name|getStartRow
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|Bytes
operator|.
name|equals
argument_list|(
name|localStartKey
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
condition|)
block|{
name|locateTheClosestFrontRow
operator|=
literal|false
expr_stmt|;
block|}
block|}
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
operator|&&
name|this
operator|.
name|currentRegion
operator|!=
literal|null
condition|)
block|{
comment|// Only worth logging if NOT first region in scan.
name|LOG
operator|.
name|debug
argument_list|(
literal|"Advancing internal scanner to startKey at '"
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|localStartKey
argument_list|)
operator|+
literal|"'"
argument_list|)
expr_stmt|;
block|}
try|try
block|{
comment|// In reversed scan, we want to locate the previous region through current
comment|// region's start key. In order to get that previous region, first we
comment|// create a closest row before the start key of current region, then
comment|// locate all the regions from the created closest row to start key of
comment|// current region, thus the last one of located regions should be the
comment|// previous region of current region. The related logic of locating
comment|// regions is implemented in ReversedScannerCallable
name|byte
index|[]
name|locateStartRow
init|=
name|locateTheClosestFrontRow
condition|?
name|createClosestRowBefore
argument_list|(
name|localStartKey
argument_list|)
else|:
literal|null
decl_stmt|;
name|callable
operator|=
name|getScannerCallable
argument_list|(
name|localStartKey
argument_list|,
name|nbRows
argument_list|,
name|locateStartRow
argument_list|)
expr_stmt|;
comment|// Open a scanner on the region server starting at the
comment|// beginning of the region
comment|// callWithoutRetries is at this layer. Within the ScannerCallableWithReplicas,
comment|// we do a callWithRetries
name|this
operator|.
name|caller
operator|.
name|callWithoutRetries
argument_list|(
name|callable
argument_list|,
name|scannerTimeout
argument_list|)
expr_stmt|;
name|this
operator|.
name|currentRegion
operator|=
name|callable
operator|.
name|getHRegionInfo
argument_list|()
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|scanMetrics
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|scanMetrics
operator|.
name|countOfRegions
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|ExceptionUtil
operator|.
name|rethrowIfInterrupt
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|close
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
return|return
literal|true
return|;
block|}
specifier|protected
name|ScannerCallableWithReplicas
name|getScannerCallable
parameter_list|(
name|byte
index|[]
name|localStartKey
parameter_list|,
name|int
name|nbRows
parameter_list|,
name|byte
index|[]
name|locateStartRow
parameter_list|)
block|{
name|scan
operator|.
name|setStartRow
argument_list|(
name|localStartKey
argument_list|)
expr_stmt|;
name|ScannerCallable
name|s
init|=
operator|new
name|ReversedScannerCallable
argument_list|(
name|getConnection
argument_list|()
argument_list|,
name|getTable
argument_list|()
argument_list|,
name|scan
argument_list|,
name|this
operator|.
name|scanMetrics
argument_list|,
name|locateStartRow
argument_list|,
name|this
operator|.
name|rpcControllerFactory
argument_list|)
decl_stmt|;
name|s
operator|.
name|setCaching
argument_list|(
name|nbRows
argument_list|)
expr_stmt|;
name|ScannerCallableWithReplicas
name|sr
init|=
operator|new
name|ScannerCallableWithReplicas
argument_list|(
name|getTable
argument_list|()
argument_list|,
name|getConnection
argument_list|()
argument_list|,
name|s
argument_list|,
name|pool
argument_list|,
name|primaryOperationTimeout
argument_list|,
name|scan
argument_list|,
name|getRetries
argument_list|()
argument_list|,
name|getScannerTimeout
argument_list|()
argument_list|,
name|caching
argument_list|,
name|getConf
argument_list|()
argument_list|,
name|caller
argument_list|)
decl_stmt|;
return|return
name|sr
return|;
block|}
annotation|@
name|Override
comment|// returns true if stopRow>= passed region startKey
specifier|protected
name|boolean
name|checkScanStopRow
parameter_list|(
specifier|final
name|byte
index|[]
name|startKey
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|scan
operator|.
name|getStopRow
argument_list|()
operator|.
name|length
operator|>
literal|0
condition|)
block|{
comment|// there is a stop row, check to see if we are past it.
name|byte
index|[]
name|stopRow
init|=
name|scan
operator|.
name|getStopRow
argument_list|()
decl_stmt|;
name|int
name|cmp
init|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|stopRow
argument_list|,
literal|0
argument_list|,
name|stopRow
operator|.
name|length
argument_list|,
name|startKey
argument_list|,
literal|0
argument_list|,
name|startKey
operator|.
name|length
argument_list|)
decl_stmt|;
if|if
condition|(
name|cmp
operator|>=
literal|0
condition|)
block|{
comment|// stopRow>= startKey (stopRow is equals to or larger than endKey)
comment|// This is a stop.
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
comment|// unlikely.
block|}
comment|/**    * Create the closest row before the specified row    * @param row    * @return a new byte array which is the closest front row of the specified one    */
specifier|protected
specifier|static
name|byte
index|[]
name|createClosestRowBefore
parameter_list|(
name|byte
index|[]
name|row
parameter_list|)
block|{
if|if
condition|(
name|row
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"The passed row is empty"
argument_list|)
throw|;
block|}
if|if
condition|(
name|Bytes
operator|.
name|equals
argument_list|(
name|row
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
condition|)
block|{
return|return
name|MAX_BYTE_ARRAY
return|;
block|}
if|if
condition|(
name|row
index|[
name|row
operator|.
name|length
operator|-
literal|1
index|]
operator|==
literal|0
condition|)
block|{
return|return
name|Arrays
operator|.
name|copyOf
argument_list|(
name|row
argument_list|,
name|row
operator|.
name|length
operator|-
literal|1
argument_list|)
return|;
block|}
else|else
block|{
name|byte
index|[]
name|closestFrontRow
init|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|row
argument_list|,
name|row
operator|.
name|length
argument_list|)
decl_stmt|;
name|closestFrontRow
index|[
name|row
operator|.
name|length
operator|-
literal|1
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
operator|(
name|closestFrontRow
index|[
name|row
operator|.
name|length
operator|-
literal|1
index|]
operator|&
literal|0xff
operator|)
operator|-
literal|1
argument_list|)
expr_stmt|;
name|closestFrontRow
operator|=
name|Bytes
operator|.
name|add
argument_list|(
name|closestFrontRow
argument_list|,
name|MAX_BYTE_ARRAY
argument_list|)
expr_stmt|;
return|return
name|closestFrontRow
return|;
block|}
block|}
block|}
end_class

end_unit

