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
name|rest
operator|.
name|filter
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
name|filter
operator|.
name|RowFilterInterface
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
name|rest
operator|.
name|exception
operator|.
name|HBaseRestException
import|;
end_import

begin_comment
comment|/**  * Constructs Filters from JSON. Filters are defined  * as JSON Objects of the form:  * {  *  "type" : "FILTER_CLASS_NAME",  * "args" : "FILTER_ARGUMENTS"  * }  *   * For Filters like WhileMatchRowFilter,  * nested Filters are supported. Just serialize a different  * filter in the form (for instance if you wanted to use WhileMatchRowFilter  * with a StopRowFilter:  *   * {  *  "type" : "WhileMatchRowFilter",  * "args" : {  *              "type" : "StopRowFilter",  *              "args" : "ROW_KEY_TO_STOP_ON"  *            }  * }  *   * For filters like RowSetFilter, nested Filters AND Filter arrays  * are supported. So for instance If one wanted to do a RegExp  * RowFilter UNIONed with a WhileMatchRowFilter(StopRowFilter),  * you would look like this:  *   * {  *   "type" : "RowFilterSet",  *   "args" : [  *                {  *                  "type" : "RegExpRowFilter",  *                  "args" : "MY_REGULAR_EXPRESSION"  *                },  *                {  *                  "type" : "WhileMatchRowFilter"  *                  "args" : {  *                                "type" : "StopRowFilter"  *                                "args" : "MY_STOP_ROW_EXPRESSION"  *                             }  *                }  *              ]  * }  */
end_comment

begin_interface
specifier|public
interface|interface
name|FilterFactory
extends|extends
name|FilterFactoryConstants
block|{
specifier|public
name|RowFilterInterface
name|getFilterFromJSON
parameter_list|(
name|String
name|args
parameter_list|)
throws|throws
name|HBaseRestException
function_decl|;
block|}
end_interface

end_unit

