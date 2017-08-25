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
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|ConnectionUtils
operator|.
name|createCloseRowBefore
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|ConnectionUtils
operator|.
name|incRPCRetriesMetrics
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|ConnectionUtils
operator|.
name|isEmptyStartRow
import|;
end_import

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
name|io
operator|.
name|InterruptedIOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|DoNotRetryIOException
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
name|HRegionLocation
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
name|RegionLocations
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
name|client
operator|.
name|metrics
operator|.
name|ScanMetrics
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

begin_comment
comment|/**  * A reversed ScannerCallable which supports backward scanning.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ReversedScannerCallable
extends|extends
name|ScannerCallable
block|{
comment|/**    * @param connection    * @param tableName    * @param scan    * @param scanMetrics    * @param rpcFactory to create an {@link com.google.protobuf.RpcController} to talk to the    *          regionserver    */
specifier|public
name|ReversedScannerCallable
parameter_list|(
name|ClusterConnection
name|connection
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|Scan
name|scan
parameter_list|,
name|ScanMetrics
name|scanMetrics
parameter_list|,
name|RpcControllerFactory
name|rpcFactory
parameter_list|)
block|{
name|super
argument_list|(
name|connection
argument_list|,
name|tableName
argument_list|,
name|scan
argument_list|,
name|scanMetrics
argument_list|,
name|rpcFactory
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param connection    * @param tableName    * @param scan    * @param scanMetrics    * @param rpcFactory to create an {@link com.google.protobuf.RpcController} to talk to the    *          regionserver    * @param replicaId the replica id    */
specifier|public
name|ReversedScannerCallable
parameter_list|(
name|ClusterConnection
name|connection
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|Scan
name|scan
parameter_list|,
name|ScanMetrics
name|scanMetrics
parameter_list|,
name|RpcControllerFactory
name|rpcFactory
parameter_list|,
name|int
name|replicaId
parameter_list|)
block|{
name|super
argument_list|(
name|connection
argument_list|,
name|tableName
argument_list|,
name|scan
argument_list|,
name|scanMetrics
argument_list|,
name|rpcFactory
argument_list|,
name|replicaId
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param reload force reload of server location    * @throws IOException    */
annotation|@
name|Override
specifier|public
name|void
name|prepare
parameter_list|(
name|boolean
name|reload
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|Thread
operator|.
name|interrupted
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|InterruptedIOException
argument_list|()
throw|;
block|}
if|if
condition|(
operator|!
name|instantiated
operator|||
name|reload
condition|)
block|{
comment|// we should use range locate if
comment|// 1. we do not want the start row
comment|// 2. the start row is empty which means we need to locate to the last region.
if|if
condition|(
name|scan
operator|.
name|includeStartRow
argument_list|()
operator|&&
operator|!
name|isEmptyStartRow
argument_list|(
name|getRow
argument_list|()
argument_list|)
condition|)
block|{
comment|// Just locate the region with the row
name|RegionLocations
name|rl
init|=
name|RpcRetryingCallerWithReadReplicas
operator|.
name|getRegionLocations
argument_list|(
operator|!
name|reload
argument_list|,
name|id
argument_list|,
name|getConnection
argument_list|()
argument_list|,
name|getTableName
argument_list|()
argument_list|,
name|getRow
argument_list|()
argument_list|)
decl_stmt|;
name|this
operator|.
name|location
operator|=
name|id
operator|<
name|rl
operator|.
name|size
argument_list|()
condition|?
name|rl
operator|.
name|getRegionLocation
argument_list|(
name|id
argument_list|)
else|:
literal|null
expr_stmt|;
if|if
condition|(
name|location
operator|==
literal|null
operator|||
name|location
operator|.
name|getServerName
argument_list|()
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to find location, tableName="
operator|+
name|getTableName
argument_list|()
operator|+
literal|", row="
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|getRow
argument_list|()
argument_list|)
operator|+
literal|", reload="
operator|+
name|reload
argument_list|)
throw|;
block|}
block|}
else|else
block|{
comment|// Need to locate the regions with the range, and the target location is
comment|// the last one which is the previous region of last region scanner
name|byte
index|[]
name|locateStartRow
init|=
name|createCloseRowBefore
argument_list|(
name|getRow
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|locatedRegions
init|=
name|locateRegionsInRange
argument_list|(
name|locateStartRow
argument_list|,
name|getRow
argument_list|()
argument_list|,
name|reload
argument_list|)
decl_stmt|;
if|if
condition|(
name|locatedRegions
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"Does hbase:meta exist hole? Couldn't get regions for the range from "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|locateStartRow
argument_list|)
operator|+
literal|" to "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|getRow
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
name|this
operator|.
name|location
operator|=
name|locatedRegions
operator|.
name|get
argument_list|(
name|locatedRegions
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
name|setStub
argument_list|(
name|getConnection
argument_list|()
operator|.
name|getClient
argument_list|(
name|getLocation
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|checkIfRegionServerIsRemote
argument_list|()
expr_stmt|;
name|instantiated
operator|=
literal|true
expr_stmt|;
block|}
comment|// check how often we retry.
if|if
condition|(
name|reload
condition|)
block|{
name|incRPCRetriesMetrics
argument_list|(
name|scanMetrics
argument_list|,
name|isRegionServerRemote
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Get the corresponding regions for an arbitrary range of keys.    * @param startKey Starting row in range, inclusive    * @param endKey Ending row in range, exclusive    * @param reload force reload of server location    * @return A list of HRegionLocation corresponding to the regions that contain    *         the specified range    * @throws IOException    */
specifier|private
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|locateRegionsInRange
parameter_list|(
name|byte
index|[]
name|startKey
parameter_list|,
name|byte
index|[]
name|endKey
parameter_list|,
name|boolean
name|reload
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|boolean
name|endKeyIsEndOfTable
init|=
name|Bytes
operator|.
name|equals
argument_list|(
name|endKey
argument_list|,
name|HConstants
operator|.
name|EMPTY_END_ROW
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|Bytes
operator|.
name|compareTo
argument_list|(
name|startKey
argument_list|,
name|endKey
argument_list|)
operator|>
literal|0
operator|)
operator|&&
operator|!
name|endKeyIsEndOfTable
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid range: "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|startKey
argument_list|)
operator|+
literal|"> "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|endKey
argument_list|)
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|regionList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|byte
index|[]
name|currentKey
init|=
name|startKey
decl_stmt|;
do|do
block|{
name|RegionLocations
name|rl
init|=
name|RpcRetryingCallerWithReadReplicas
operator|.
name|getRegionLocations
argument_list|(
operator|!
name|reload
argument_list|,
name|id
argument_list|,
name|getConnection
argument_list|()
argument_list|,
name|getTableName
argument_list|()
argument_list|,
name|currentKey
argument_list|)
decl_stmt|;
name|HRegionLocation
name|regionLocation
init|=
name|id
operator|<
name|rl
operator|.
name|size
argument_list|()
condition|?
name|rl
operator|.
name|getRegionLocation
argument_list|(
name|id
argument_list|)
else|:
literal|null
decl_stmt|;
if|if
condition|(
name|regionLocation
operator|!=
literal|null
operator|&&
name|regionLocation
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|containsRow
argument_list|(
name|currentKey
argument_list|)
condition|)
block|{
name|regionList
operator|.
name|add
argument_list|(
name|regionLocation
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"Does hbase:meta exist hole? Locating row "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|currentKey
argument_list|)
operator|+
literal|" returns incorrect region "
operator|+
operator|(
name|regionLocation
operator|==
literal|null
condition|?
literal|null
else|:
name|regionLocation
operator|.
name|getRegionInfo
argument_list|()
operator|)
argument_list|)
throw|;
block|}
name|currentKey
operator|=
name|regionLocation
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getEndKey
argument_list|()
expr_stmt|;
block|}
do|while
condition|(
operator|!
name|Bytes
operator|.
name|equals
argument_list|(
name|currentKey
argument_list|,
name|HConstants
operator|.
name|EMPTY_END_ROW
argument_list|)
operator|&&
operator|(
name|endKeyIsEndOfTable
operator|||
name|Bytes
operator|.
name|compareTo
argument_list|(
name|currentKey
argument_list|,
name|endKey
argument_list|)
operator|<
literal|0
operator|)
condition|)
do|;
return|return
name|regionList
return|;
block|}
annotation|@
name|Override
specifier|public
name|ScannerCallable
name|getScannerCallableForReplica
parameter_list|(
name|int
name|id
parameter_list|)
block|{
name|ReversedScannerCallable
name|r
init|=
operator|new
name|ReversedScannerCallable
argument_list|(
name|getConnection
argument_list|()
argument_list|,
name|getTableName
argument_list|()
argument_list|,
name|this
operator|.
name|getScan
argument_list|()
argument_list|,
name|this
operator|.
name|scanMetrics
argument_list|,
name|rpcControllerFactory
argument_list|,
name|id
argument_list|)
decl_stmt|;
name|r
operator|.
name|setCaching
argument_list|(
name|this
operator|.
name|getCaching
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|r
return|;
block|}
block|}
end_class

end_unit

