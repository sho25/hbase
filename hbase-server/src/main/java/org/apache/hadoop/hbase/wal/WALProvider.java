begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|wal
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

begin_comment
comment|// imports for things that haven't moved from regionserver.wal yet.
end_comment

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
name|regionserver
operator|.
name|wal
operator|.
name|WALActionsListener
import|;
end_import

begin_comment
comment|/**  * The Write Ahead Log (WAL) stores all durable edits to the HRegion.  * This interface provides the entry point for all WAL implementors.  *<p>  * See {@link DefaultWALProvider} for an example implementation.  *  * A single WALProvider will be used for retrieving multiple WALs in a particular region server  * and must be threadsafe.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|WALProvider
block|{
comment|/**    * Set up the provider to create wals.    * will only be called once per instance.    * @param factory factory that made us may not be null    * @param conf may not be null    * @param listeners may be null    * @param providerId differentiate between providers from one factory. may be null    */
name|void
name|init
parameter_list|(
specifier|final
name|WALFactory
name|factory
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|List
argument_list|<
name|WALActionsListener
argument_list|>
name|listeners
parameter_list|,
specifier|final
name|String
name|providerId
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * @param identifier may not be null. contents will not be altered.    * @return a WAL for writing entries for the given region.    */
name|WAL
name|getWAL
parameter_list|(
specifier|final
name|byte
index|[]
name|identifier
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * persist outstanding WALs to storage and stop accepting new appends.    * This method serves as shorthand for sending a sync to every WAL provided by a given    * implementation. Those WALs will also stop accepting new writes.    */
name|void
name|shutdown
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * shutdown utstanding WALs and clean up any persisted state.    * Call this method only when you will not need to replay any of the edits to the WALs from    * this provider. After this call completes, the underlying resources should have been reclaimed.    */
name|void
name|close
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|// Writers are used internally. Users outside of the WAL should be relying on the
comment|// interface provided by WAL.
interface|interface
name|Writer
extends|extends
name|Closeable
block|{
name|void
name|sync
parameter_list|()
throws|throws
name|IOException
function_decl|;
name|void
name|append
parameter_list|(
name|WAL
operator|.
name|Entry
name|entry
parameter_list|)
throws|throws
name|IOException
function_decl|;
name|long
name|getLength
parameter_list|()
throws|throws
name|IOException
function_decl|;
block|}
block|}
end_interface

end_unit

