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
name|mapreduce
package|;
end_package

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
name|Put
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
name|ImmutableBytesWritable
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
name|mapreduce
operator|.
name|Reducer
import|;
end_import

begin_comment
comment|/**  * Extends the basic<code>Reducer</code> class to add the required key and  * value output classes.  *   * @param<KEYIN>  The type of the key.  * @param<VALUEIN>  The type of the value.  * @see org.apache.hadoop.mapreduce.Reducer  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|TableReducer
parameter_list|<
name|KEYIN
parameter_list|,
name|VALUEIN
parameter_list|>
extends|extends
name|Reducer
argument_list|<
name|KEYIN
argument_list|,
name|VALUEIN
argument_list|,
name|ImmutableBytesWritable
argument_list|,
name|Put
argument_list|>
block|{  }
end_class

end_unit

