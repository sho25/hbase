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
name|coprocessor
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
name|java
operator|.
name|util
operator|.
name|UUID
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
name|KeyValue
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
name|client
operator|.
name|Scan
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|Writable
import|;
end_import

begin_comment
comment|/**  * Defines the procedure to atomically perform multiple scans and mutations  * on one single row. The generic type parameter T is the return type of  * RowProcessor.getResult().  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
interface|interface
name|RowProcessor
parameter_list|<
name|T
parameter_list|>
extends|extends
name|Writable
block|{
comment|/**    * Which row to perform the read-write    */
name|byte
index|[]
name|getRow
parameter_list|()
function_decl|;
comment|/**    * Obtain the processing result    */
name|T
name|getResult
parameter_list|()
function_decl|;
comment|/**    * Is this operation read only? If this is true, process() should not add    * any mutations or it throws IOException.    * @return ture if read only operation    */
name|boolean
name|readOnly
parameter_list|()
function_decl|;
comment|/**    * HRegion calls this to process a row. You should override this to create    * your own RowProcessor.    *    * @param now the current system millisecond    * @param scanner the call back object the can be used to scan the row    * @param mutations the mutations for HRegion to do    * @param walEdit the wal edit here allows inject some other meta data    */
name|void
name|process
parameter_list|(
name|long
name|now
parameter_list|,
name|RowProcessor
operator|.
name|RowScanner
name|scanner
parameter_list|,
name|List
argument_list|<
name|KeyValue
argument_list|>
name|mutations
parameter_list|,
name|WALEdit
name|walEdit
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * The call back provided by HRegion to perform the scans on the row    */
specifier|public
interface|interface
name|RowScanner
block|{
comment|/**      * @param scan The object defines what to read      * @param result The scan results will be added here      */
name|void
name|doScan
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|List
argument_list|<
name|KeyValue
argument_list|>
name|result
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
comment|/**    * @return The replication cluster id.    */
name|UUID
name|getClusterId
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

