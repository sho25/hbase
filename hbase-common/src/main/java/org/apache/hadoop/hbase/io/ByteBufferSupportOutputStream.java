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
name|io
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
name|hbase
operator|.
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Interface adds support for writing {@link ByteBuffer} into OutputStream.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|ByteBufferSupportOutputStream
block|{
comment|/**    * Writes<code>len</code> bytes from the specified ByteBuffer starting at offset<code>off</code>    * to this output stream.    *    * @param b the data.    * @param off the start offset in the data.    * @param len the number of bytes to write.    * @exception IOException    *              if an I/O error occurs. In particular, an<code>IOException</code> is thrown if    *              the output stream is closed.    */
name|void
name|write
parameter_list|(
name|ByteBuffer
name|b
parameter_list|,
name|int
name|off
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Writes an<code>int</code> to the underlying output stream as four    * bytes, high byte first.    * @param i the<code>int</code> to write    * @throws IOException if an I/O error occurs.    */
name|void
name|writeInt
parameter_list|(
name|int
name|i
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

