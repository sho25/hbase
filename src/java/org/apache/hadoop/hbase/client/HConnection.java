begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ArrayList
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
name|HServerAddress
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
name|HTableDescriptor
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
name|MasterNotRunningException
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
name|HMasterInterface
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
name|HRegionInterface
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
name|zookeeper
operator|.
name|ZooKeeperWrapper
import|;
end_import

begin_comment
comment|/**  * Cluster connection.  * {@link HConnectionManager} manages instances of this class.  */
end_comment

begin_interface
specifier|public
interface|interface
name|HConnection
block|{
comment|/**    * Retrieve ZooKeeperWrapper used by the connection.    * @return ZooKeeperWrapper handle being used by the connection.    * @throws IOException    */
specifier|public
name|ZooKeeperWrapper
name|getZooKeeperWrapper
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * @return proxy connection to master server for this instance    * @throws MasterNotRunningException    */
specifier|public
name|HMasterInterface
name|getMaster
parameter_list|()
throws|throws
name|MasterNotRunningException
function_decl|;
comment|/** @return - true if the master server is running */
specifier|public
name|boolean
name|isMasterRunning
parameter_list|()
function_decl|;
comment|/**    * Checks if<code>tableName</code> exists.    * @param tableName Table to check.    * @return True if table exists already.    * @throws MasterNotRunningException    */
specifier|public
name|boolean
name|tableExists
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|MasterNotRunningException
function_decl|;
comment|/**    * A table that isTableEnabled == false and isTableDisabled == false    * is possible. This happens when a table has a lot of regions    * that must be processed.    * @param tableName    * @return true if the table is enabled, false otherwise    * @throws IOException    */
specifier|public
name|boolean
name|isTableEnabled
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * @param tableName    * @return true if the table is disabled, false otherwise    * @throws IOException    */
specifier|public
name|boolean
name|isTableDisabled
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * List all the userspace tables.  In other words, scan the META table.    *    * If we wanted this to be really fast, we could implement a special    * catalog table that just contains table names and their descriptors.    * Right now, it only exists as part of the META table's region info.    *    * @return - returns an array of HTableDescriptors     * @throws IOException    */
specifier|public
name|HTableDescriptor
index|[]
name|listTables
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * @param tableName    * @return table metadata     * @throws IOException    */
specifier|public
name|HTableDescriptor
name|getHTableDescriptor
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Find the location of the region of<i>tableName</i> that<i>row</i>    * lives in.    * @param tableName name of the table<i>row</i> is in    * @param row row key you're trying to find the region of    * @return HRegionLocation that describes where to find the reigon in     * question    * @throws IOException    */
specifier|public
name|HRegionLocation
name|locateRegion
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Find the location of the region of<i>tableName</i> that<i>row</i>    * lives in, ignoring any value that might be in the cache.    * @param tableName name of the table<i>row</i> is in    * @param row row key you're trying to find the region of    * @return HRegionLocation that describes where to find the reigon in     * question    * @throws IOException    */
specifier|public
name|HRegionLocation
name|relocateRegion
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**     * Establishes a connection to the region server at the specified address.    * @param regionServer - the server to connect to    * @return proxy for HRegionServer    * @throws IOException    */
specifier|public
name|HRegionInterface
name|getHRegionConnection
parameter_list|(
name|HServerAddress
name|regionServer
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**     * Establishes a connection to the region server at the specified address.    * @param regionServer - the server to connect to    * @param getMaster - do we check if master is alive    * @return proxy for HRegionServer    * @throws IOException    */
specifier|public
name|HRegionInterface
name|getHRegionConnection
parameter_list|(
name|HServerAddress
name|regionServer
parameter_list|,
name|boolean
name|getMaster
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Find region location hosting passed row    * @param tableName    * @param row Row to find.    * @param reload If true do not use cache, otherwise bypass.    * @return Location of row.    * @throws IOException    */
name|HRegionLocation
name|getRegionLocation
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|boolean
name|reload
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Pass in a ServerCallable with your particular bit of logic defined and     * this method will manage the process of doing retries with timed waits     * and refinds of missing regions.    *    * @param<T> the type of the return value    * @param callable    * @return an object of type T    * @throws IOException    * @throws RuntimeException    */
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
name|getRegionServerWithRetries
parameter_list|(
name|ServerCallable
argument_list|<
name|T
argument_list|>
name|callable
parameter_list|)
throws|throws
name|IOException
throws|,
name|RuntimeException
function_decl|;
comment|/**    * Pass in a ServerCallable with your particular bit of logic defined and    * this method will pass it to the defined region server.    * @param<T> the type of the return value    * @param callable    * @return an object of type T    * @throws IOException    * @throws RuntimeException    */
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
name|getRegionServerForWithoutRetries
parameter_list|(
name|ServerCallable
argument_list|<
name|T
argument_list|>
name|callable
parameter_list|)
throws|throws
name|IOException
throws|,
name|RuntimeException
function_decl|;
comment|/**    * Process a batch of rows. Currently it only works for updates until     * HBASE-880 is available. Does the retries.    * @param list A batch of rows to process    * @param tableName The name of the table    * @throws IOException    */
specifier|public
name|void
name|processBatchOfRows
parameter_list|(
name|ArrayList
argument_list|<
name|Put
argument_list|>
name|list
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

