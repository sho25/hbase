begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright (c) 2005, European Commission project OneLab under contract 034819   * (http://www.one-lab.org)  *   * All rights reserved.  * Redistribution and use in source and binary forms, with or   * without modification, are permitted provided that the following   * conditions are met:  *  - Redistributions of source code must retain the above copyright   *    notice, this list of conditions and the following disclaimer.  *  - Redistributions in binary form must reproduce the above copyright   *    notice, this list of conditions and the following disclaimer in   *    the documentation and/or other materials provided with the distribution.  *  - Neither the name of the University Catholique de Louvain - UCL  *    nor the names of its contributors may be used to endorse or   *    promote products derived from this software without specific prior   *    written permission.  *      * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS   * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT   * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS   * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE   * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,   * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,   * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;   * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER   * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT   * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN   * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE   * POSSIBILITY OF SUCH DAMAGE.  */
end_comment

begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|onelab
operator|.
name|test
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|UnsupportedEncodingException
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
name|HConstants
import|;
end_import

begin_import
import|import
name|org
operator|.
name|onelab
operator|.
name|filter
operator|.
name|Key
import|;
end_import

begin_comment
comment|/**  * Test class for keys.  *<p>  * It gives an example on how to extend Key.  *   * contract<a href="http://www.one-lab.org">European Commission One-Lab Project 034819</a>.  *  * @version 1.0 - 5 Feb. 07  *   * @see org.onelab.filter.Key A key stored in a filter  */
end_comment

begin_class
specifier|public
class|class
name|StringKey
extends|extends
name|Key
block|{
comment|/** Default constructor - use with readFields */
specifier|public
name|StringKey
parameter_list|()
block|{}
comment|/**    * Construct a Key using the specified String and default weight    *     * @param key String key value    * @throws UnsupportedEncodingException    */
specifier|public
name|StringKey
parameter_list|(
name|String
name|key
parameter_list|)
throws|throws
name|UnsupportedEncodingException
block|{
name|super
argument_list|(
name|key
operator|.
name|getBytes
argument_list|(
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Construct a Key using the specified string and weight    *     * @param key - String key value    * @param weight key weight    * @throws UnsupportedEncodingException    */
specifier|public
name|StringKey
parameter_list|(
name|String
name|key
parameter_list|,
name|double
name|weight
parameter_list|)
throws|throws
name|UnsupportedEncodingException
block|{
name|super
argument_list|(
name|key
operator|.
name|getBytes
argument_list|(
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|,
name|weight
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

