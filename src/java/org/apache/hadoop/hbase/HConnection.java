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
name|SortedMap
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
name|io
operator|.
name|Text
import|;
end_import

begin_comment
comment|/**  *   */
end_comment

begin_interface
specifier|public
interface|interface
name|HConnection
block|{
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
comment|/**    * Checks if<code>tableName</code> exists.    * @param tableName Table to check.    * @return True if table exists already.    */
specifier|public
name|boolean
name|tableExists
parameter_list|(
specifier|final
name|Text
name|tableName
parameter_list|)
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
comment|/**    * Find the location of the region of<i>tableName</i> that<i>row</i>    * lives in.    * @param tableName name of the table<i>row</i> is in    * @param row row key you're trying to find the region of    * @return HRegionLocation that describes where to find the reigon in     * question    */
specifier|public
name|HRegionLocation
name|locateRegion
parameter_list|(
name|Text
name|tableName
parameter_list|,
name|Text
name|row
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Find the location of the region of<i>tableName</i> that<i>row</i>    * lives in, ignoring any value that might be in the cache.    * @param tableName name of the table<i>row</i> is in    * @param row row key you're trying to find the region of    * @return HRegionLocation that describes where to find the reigon in     * question    */
specifier|public
name|HRegionLocation
name|relocateRegion
parameter_list|(
name|Text
name|tableName
parameter_list|,
name|Text
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
block|}
end_interface

end_unit

