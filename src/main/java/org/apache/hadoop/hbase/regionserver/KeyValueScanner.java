begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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

begin_comment
comment|/**  * Scanner that returns the next KeyValue.  */
end_comment

begin_interface
specifier|public
interface|interface
name|KeyValueScanner
block|{
comment|/**    * Look at the next KeyValue in this scanner, but do not iterate scanner.    * @return the next KeyValue    */
specifier|public
name|KeyValue
name|peek
parameter_list|()
function_decl|;
comment|/**    * Return the next KeyValue in this scanner, iterating the scanner    * @return the next KeyValue    */
specifier|public
name|KeyValue
name|next
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Seek the scanner at or after the specified KeyValue.    * @param key seek value    * @return true if scanner has values left, false if end of scanner    */
specifier|public
name|boolean
name|seek
parameter_list|(
name|KeyValue
name|key
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Reseek the scanner at or after the specified KeyValue.    * This method is guaranteed to seek to or before the required key only if the    * key comes after the current position of the scanner. Should not be used    * to seek to a key which may come before the current position.    * @param key seek value (should be non-null)    * @return true if scanner has values left, false if end of scanner    */
specifier|public
name|boolean
name|reseek
parameter_list|(
name|KeyValue
name|key
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Get the sequence id associated with this KeyValueScanner. This is required    * for comparing multiple files to find out which one has the latest data.    * The default implementation for this would be to return 0. A file having    * lower sequence id will be considered to be the older one.    */
specifier|public
name|long
name|getSequenceID
parameter_list|()
function_decl|;
comment|/**    * Close the KeyValue scanner.    */
specifier|public
name|void
name|close
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

