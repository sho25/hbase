begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|regionserver
operator|.
name|wal
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|Path
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
name|HRegionInfo
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

begin_comment
comment|/**  * Get notification of {@link HLog}/WAL log events. The invocations are inline  * so make sure your implementation is fast else you'll slow hbase.  */
end_comment

begin_interface
specifier|public
interface|interface
name|WALObserver
block|{
comment|/**    * The WAL was rolled.    * @param newFile the path to the new hlog    */
specifier|public
name|void
name|logRolled
parameter_list|(
name|Path
name|newFile
parameter_list|)
function_decl|;
comment|/**    * A request was made that the WAL be rolled.    */
specifier|public
name|void
name|logRollRequested
parameter_list|()
function_decl|;
comment|/**    * The WAL is about to close.    */
specifier|public
name|void
name|logCloseRequested
parameter_list|()
function_decl|;
comment|/**   * Called before each write.   * @param info   * @param logKey   * @param logEdit   */
specifier|public
name|void
name|visitLogEntryBeforeWrite
parameter_list|(
name|HRegionInfo
name|info
parameter_list|,
name|HLogKey
name|logKey
parameter_list|,
name|WALEdit
name|logEdit
parameter_list|)
function_decl|;
comment|/**    *    * @param htd    * @param logKey    * @param logEdit    */
specifier|public
name|void
name|visitLogEntryBeforeWrite
parameter_list|(
name|HTableDescriptor
name|htd
parameter_list|,
name|HLogKey
name|logKey
parameter_list|,
name|WALEdit
name|logEdit
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

