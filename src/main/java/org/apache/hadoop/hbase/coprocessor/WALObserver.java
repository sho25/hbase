begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|coprocessor
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
name|regionserver
operator|.
name|wal
operator|.
name|HLogKey
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
name|regionserver
operator|.
name|wal
operator|.
name|WALEdit
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

begin_comment
comment|/**  * It's provided to have a way for coprocessors to observe, rewrite,  * or skip WALEdits as they are being written to the WAL.  *  * {@link org.apache.hadoop.hbase.coprocessor.RegionObserver} provides  * hooks for adding logic for WALEdits in the region context during reconstruction,  *  * Defines coprocessor hooks for interacting with operations on the  * {@link org.apache.hadoop.hbase.regionserver.wal.HLog}.  */
end_comment

begin_interface
specifier|public
interface|interface
name|WALObserver
extends|extends
name|Coprocessor
block|{
comment|/**    * Called before a {@link org.apache.hadoop.hbase.regionserver.wal.WALEdit}    * is writen to WAL.    *    * @param env    * @param info    * @param logKey    * @param logEdit    * @return true if default behavior should be bypassed, false otherwise    * @throws IOException    */
name|boolean
name|preWALWrite
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|,
name|HRegionInfo
name|info
parameter_list|,
name|HLogKey
name|logKey
parameter_list|,
name|WALEdit
name|logEdit
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called after a {@link org.apache.hadoop.hbase.regionserver.wal.WALEdit}    * is writen to WAL.    *    * @param env    * @param info    * @param logKey    * @param logEdit    * @throws IOException    */
name|void
name|postWALWrite
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|,
name|HRegionInfo
name|info
parameter_list|,
name|HLogKey
name|logKey
parameter_list|,
name|WALEdit
name|logEdit
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

