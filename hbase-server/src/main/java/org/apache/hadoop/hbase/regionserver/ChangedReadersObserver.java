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
name|regionserver
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
name|List
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * If set of MapFile.Readers in Store change, implementors are notified.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|ChangedReadersObserver
block|{
comment|/**    * @return the read point of the current scan    */
name|long
name|getReadPoint
parameter_list|()
function_decl|;
comment|/**    * Notify observers.    * @param sfs The new files    * @param memStoreScanners scanner of current memstore    * @throws IOException e    */
name|void
name|updateReaders
parameter_list|(
name|List
argument_list|<
name|HStoreFile
argument_list|>
name|sfs
parameter_list|,
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|memStoreScanners
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

