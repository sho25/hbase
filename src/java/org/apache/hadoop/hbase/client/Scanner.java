begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2008 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|RowResult
import|;
end_import

begin_comment
comment|/**  * Interface for client-side scanning.  * Go to {@link HTable} to obtain instances.  */
end_comment

begin_interface
specifier|public
interface|interface
name|Scanner
extends|extends
name|Closeable
extends|,
name|Iterable
argument_list|<
name|RowResult
argument_list|>
block|{
comment|/**    * Grab the next row's worth of values. The scanner will return a RowResult    * that contains both the row's key and a map of byte[] column names to Cell     * value objects. The data returned will only contain the most recent data     * value for each row that is not newer than the target time passed when the    * scanner was created.    * @return RowResult object if there is another row, null if the scanner is    * exhausted.    * @throws IOException    */
specifier|public
name|RowResult
name|next
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * @param nbRows number of rows to return    * @return Between zero and<param>nbRows</param> RowResults    * @throws IOException    */
specifier|public
name|RowResult
index|[]
name|next
parameter_list|(
name|int
name|nbRows
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Closes the scanner and releases any resources it has allocated    */
specifier|public
name|void
name|close
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

