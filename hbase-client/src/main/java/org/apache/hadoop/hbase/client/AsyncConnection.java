begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Closeable
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
name|classification
operator|.
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * The asynchronous version of Connection.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Unstable
specifier|public
interface|interface
name|AsyncConnection
extends|extends
name|Closeable
block|{
comment|/**    * Returns the {@link org.apache.hadoop.conf.Configuration} object used by this instance.    *<p>    * The reference returned is not a copy, so any change made to it will affect this instance.    */
name|Configuration
name|getConfiguration
parameter_list|()
function_decl|;
comment|/**    * Retrieve a AsyncRegionLocator implementation to inspect region information on a table. The    * returned AsyncRegionLocator is not thread-safe, so a new instance should be created for each    * using thread. This is a lightweight operation. Pooling or caching of the returned    * AsyncRegionLocator is neither required nor desired.    * @param tableName Name of the table who's region is to be examined    * @return An AsyncRegionLocator instance    */
name|AsyncTableRegionLocator
name|getRegionLocator
parameter_list|(
name|TableName
name|tableName
parameter_list|)
function_decl|;
comment|/**    * Retrieve an RawAsyncTable implementation for accessing a table. The returned Table is not    * thread safe, a new instance should be created for each using thread. This is a lightweight    * operation, pooling or caching of the returned AsyncTable is neither required nor desired.    *<p>    * This method no longer checks table existence. An exception will be thrown if the table does not    * exist only when the first operation is attempted.    * @param tableName the name of the table    * @return an RawAsyncTable to use for interactions with this table    */
name|RawAsyncTable
name|getRawTable
parameter_list|(
name|TableName
name|tableName
parameter_list|)
function_decl|;
comment|/**    * Retrieve an AsyncTable implementation for accessing a table. The returned Table is not thread    * safe, a new instance should be created for each using thread. This is a lightweight operation,    * pooling or caching of the returned AsyncTable is neither required nor desired.    *<p>    * This method no longer checks table existence. An exception will be thrown if the table does not    * exist only when the first operation is attempted.    * @param tableName the name of the table    * @param pool the thread pool to use for executing callback    * @return an AsyncTable to use for interactions with this table    */
name|AsyncTable
name|getTable
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|ExecutorService
name|pool
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

