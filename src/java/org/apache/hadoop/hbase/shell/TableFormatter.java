begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|shell
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
name|io
operator|.
name|Writer
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
name|shell
operator|.
name|formatter
operator|.
name|AsciiTableFormatter
import|;
end_import

begin_comment
comment|/**  * Interface implemented by table formatters outputting select results.  * Implementations must have a constructor that takes a Writer.  * @see AsciiTableFormatter  */
end_comment

begin_interface
specifier|public
interface|interface
name|TableFormatter
block|{
comment|/**    * Output header.    * @param titles Titles to emit.    * @throws IOException    */
specifier|public
name|void
name|header
parameter_list|(
specifier|final
name|String
index|[]
name|titles
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Output footer.    * @throws IOException    */
specifier|public
name|void
name|footer
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Output a row.    * @param cells    * @throws IOException    */
specifier|public
name|void
name|row
parameter_list|(
specifier|final
name|String
index|[]
name|cells
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * @return Output stream being used (This is in interface to enforce fact    * that formatters use Writers -- that they operate on character streams    * rather than on byte streams).    */
specifier|public
name|Writer
name|getOut
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

