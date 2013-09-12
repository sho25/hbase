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
name|codec
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
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
name|CellScanner
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
name|io
operator|.
name|CellOutputStream
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
name|io
operator|.
name|encoding
operator|.
name|DataBlockEncoder
import|;
end_import

begin_comment
comment|/**  * Encoder/Decoder for Cell.  *  *<p>Like {@link DataBlockEncoder} only Cell-based rather than KeyValue version 1 based  * and without presuming an hfile context.  Intent is an Interface that will work for hfile and  * rpc.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|Codec
block|{
comment|// TODO: interfacing with {@link DataBlockEncoder}
comment|/**    * Call flush when done.  Some encoders may not put anything on the stream until flush is called.    * On flush, let go of any resources used by the encoder.    */
interface|interface
name|Encoder
extends|extends
name|CellOutputStream
block|{}
comment|/**    * Implementations should implicitly clean up any resources allocated when the    * Decoder/CellScanner runs off the end of the cell block. Do this rather than require the user    * call close explicitly.    */
interface|interface
name|Decoder
extends|extends
name|CellScanner
block|{}
empty_stmt|;
name|Decoder
name|getDecoder
parameter_list|(
name|InputStream
name|is
parameter_list|)
function_decl|;
name|Encoder
name|getEncoder
parameter_list|(
name|OutputStream
name|os
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

