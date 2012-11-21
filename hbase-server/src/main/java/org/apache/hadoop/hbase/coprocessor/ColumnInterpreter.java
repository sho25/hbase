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
name|classification
operator|.
name|InterfaceStability
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
name|coprocessor
operator|.
name|LongColumnInterpreter
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
import|;
end_import

begin_comment
comment|/**  * Defines how value for specific column is interpreted and provides utility  * methods like compare, add, multiply etc for them. Takes column family, column  * qualifier and return the cell value. Its concrete implementation should  * handle null case gracefully. Refer to {@link LongColumnInterpreter} for an  * example.  *<p>  * Takes two generic parameters. The cell value type of the interpreter is<T>.  * During some computations like sum, average, the return type can be different  * than the cell value data type, for eg, sum of int cell values might overflow  * in case of a int result, we should use Long for its result. Therefore, this  * class mandates to use a different (promoted) data type for result of these  * computations<S>. All computations are performed on the promoted data type  *<S>. There is a conversion method  * {@link ColumnInterpreter#castToReturnType(Object)} which takes a<T> type and  * returns a<S> type.  * @param<T> Cell value data type  * @param<S> Promoted data type  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
interface|interface
name|ColumnInterpreter
parameter_list|<
name|T
parameter_list|,
name|S
parameter_list|>
block|{
comment|/**    * @param colFamily    * @param colQualifier    * @param kv    * @return value of type T    * @throws IOException    */
name|T
name|getValue
parameter_list|(
name|byte
index|[]
name|colFamily
parameter_list|,
name|byte
index|[]
name|colQualifier
parameter_list|,
name|KeyValue
name|kv
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * @param l1    * @param l2    * @return sum or non null value among (if either of them is null); otherwise    * returns a null.    */
specifier|public
name|S
name|add
parameter_list|(
name|S
name|l1
parameter_list|,
name|S
name|l2
parameter_list|)
function_decl|;
comment|/**    * returns the maximum value for this type T    * @return max    */
name|T
name|getMaxValue
parameter_list|()
function_decl|;
name|T
name|getMinValue
parameter_list|()
function_decl|;
comment|/**    * @param o1    * @param o2    * @return multiplication    */
name|S
name|multiply
parameter_list|(
name|S
name|o1
parameter_list|,
name|S
name|o2
parameter_list|)
function_decl|;
comment|/**    * @param o    * @return increment    */
name|S
name|increment
parameter_list|(
name|S
name|o
parameter_list|)
function_decl|;
comment|/**    * provides casting opportunity between the data types.    * @param o    * @return cast    */
name|S
name|castToReturnType
parameter_list|(
name|T
name|o
parameter_list|)
function_decl|;
comment|/**    * This takes care if either of arguments are null. returns 0 if they are    * equal or both are null;    *<ul>    *<li>>0 if l1> l2 or l1 is not null and l2 is null.    *<li>< 0 if l1< l2 or l1 is null and l2 is not null.    */
name|int
name|compare
parameter_list|(
specifier|final
name|T
name|l1
parameter_list|,
specifier|final
name|T
name|l2
parameter_list|)
function_decl|;
comment|/**    * used for computing average of<S> data values. Not providing the divide    * method that takes two<S> values as it is not needed as of now.    * @param o    * @param l    * @return Average    */
name|double
name|divideForAvg
parameter_list|(
name|S
name|o
parameter_list|,
name|Long
name|l
parameter_list|)
function_decl|;
comment|/**    * This method should return any additional data that is needed on the    * server side to construct the ColumnInterpreter. The server    * will pass this to the {@link #initialize(ByteString)}    * method. If there is no ColumnInterpreter specific data (for e.g.,    * {@link LongColumnInterpreter}) then null should be returned.    * @return the PB message    */
name|ByteString
name|columnInterpreterSpecificData
parameter_list|()
function_decl|;
comment|/**    * Return the PB for type T    * @param t    * @return PB-message    */
name|ByteString
name|getProtoForCellType
parameter_list|(
name|T
name|t
parameter_list|)
function_decl|;
comment|/**    * Return the PB for type S    * @param s    * @return PB-message    */
name|ByteString
name|getProtoForPromotedType
parameter_list|(
name|S
name|s
parameter_list|)
function_decl|;
comment|/**    * This method should initialize any field(s) of the ColumnInterpreter with    * a parsing of the passed message bytes (used on the server side).    * @param bytes    */
name|void
name|initialize
parameter_list|(
name|ByteString
name|bytes
parameter_list|)
function_decl|;
comment|/**    * Converts the bytes in the server's response to the expected type S    * @param response    * @return response of type S constructed from the message    */
name|S
name|parseResponseAsPromotedType
parameter_list|(
name|byte
index|[]
name|response
parameter_list|)
function_decl|;
comment|/**    * The response message comes as type S. This will convert/cast it to T.    * In some sense, performs the opposite of {@link #castToReturnType(Object)}    * @param response    * @return cast    */
name|T
name|castToCellType
parameter_list|(
name|S
name|response
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

