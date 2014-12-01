begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|Cell
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
comment|/**  * Accepts a stream of Cells. This can be used to build a block of cells during compactions  * and flushes, or to build a byte[] to send to the client. This could be backed by a  * List<KeyValue>, but more efficient implementations will append results to a  * byte[] to eliminate overhead, and possibly encode the cells further.  *<p>To read Cells, use {@link org.apache.hadoop.hbase.CellScanner}  * @see org.apache.hadoop.hbase.CellScanner  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
interface|interface
name|CellOutputStream
block|{
comment|/**    * Implementation must copy the entire state of the Cell. If the written Cell is modified    * immediately after the write method returns, the modifications must have absolutely no effect    * on the copy of the Cell that was added in the write.    * @param cell Cell to write out    * @throws IOException    */
name|void
name|write
parameter_list|(
name|Cell
name|cell
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Let the implementation decide what to do.  Usually means writing accumulated data into a    * byte[] that can then be read from the implementation to be sent to disk, put in the block    * cache, or sent over the network.    * @throws IOException    */
name|void
name|flush
parameter_list|()
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

