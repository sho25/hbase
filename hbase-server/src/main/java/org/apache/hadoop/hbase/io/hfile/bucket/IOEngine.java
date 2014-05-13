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
name|io
operator|.
name|hfile
operator|.
name|bucket
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
name|nio
operator|.
name|ByteBuffer
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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * A class implementing IOEngine interface supports data services for  * {@link BucketCache}.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|IOEngine
block|{
comment|/**    * @return true if persistent storage is supported for the cache when shutdown    */
name|boolean
name|isPersistent
parameter_list|()
function_decl|;
comment|/**    * Transfers data from IOEngine to the given byte buffer    * @param dstBuffer the given byte buffer into which bytes are to be written    * @param offset The offset in the IO engine where the first byte to be read    * @return number of bytes read    * @throws IOException    */
name|int
name|read
parameter_list|(
name|ByteBuffer
name|dstBuffer
parameter_list|,
name|long
name|offset
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Transfers data from the given byte buffer to IOEngine    * @param srcBuffer the given byte buffer from which bytes are to be read    * @param offset The offset in the IO engine where the first byte to be    *          written    * @throws IOException    */
name|void
name|write
parameter_list|(
name|ByteBuffer
name|srcBuffer
parameter_list|,
name|long
name|offset
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Sync the data to IOEngine after writing    * @throws IOException    */
name|void
name|sync
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Shutdown the IOEngine    */
name|void
name|shutdown
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

