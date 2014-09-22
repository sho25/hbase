begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Modifier
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_comment
comment|/**  * Utility class to check whether a given class conforms to builder-style:  * Foo foo =  *   new Foo()  *     .setBar(bar)  *     .setBaz(baz)  */
end_comment

begin_class
specifier|public
class|class
name|BuilderStyleTest
block|{
comment|/*    * If a base class Foo declares a method setFoo() returning Foo, then the subclass should    * re-declare the methods overriding the return class with the subclass:    *    * class Foo {    *   Foo setFoo() {    *     ..    *     return this;    *   }    * }    *    * class Bar {    *   Bar setFoo() {    *     return (Bar) super.setFoo();    *   }    * }    *    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
specifier|public
specifier|static
name|void
name|assertClassesAreBuilderStyle
parameter_list|(
name|Class
modifier|...
name|classes
parameter_list|)
block|{
for|for
control|(
name|Class
name|clazz
range|:
name|classes
control|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Checking "
operator|+
name|clazz
argument_list|)
expr_stmt|;
name|Method
index|[]
name|methods
init|=
name|clazz
operator|.
name|getDeclaredMethods
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|Method
argument_list|>
argument_list|>
name|methodsBySignature
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Method
name|method
range|:
name|methods
control|)
block|{
if|if
condition|(
operator|!
name|Modifier
operator|.
name|isPublic
argument_list|(
name|method
operator|.
name|getModifiers
argument_list|()
argument_list|)
condition|)
block|{
continue|continue;
comment|// only public classes
block|}
name|Class
argument_list|<
name|?
argument_list|>
name|ret
init|=
name|method
operator|.
name|getReturnType
argument_list|()
decl_stmt|;
if|if
condition|(
name|method
operator|.
name|getName
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"set"
argument_list|)
operator|||
name|method
operator|.
name|getName
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"add"
argument_list|)
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"  "
operator|+
name|clazz
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"."
operator|+
name|method
operator|.
name|getName
argument_list|()
operator|+
literal|"() : "
operator|+
name|ret
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
comment|// because of subclass / super class method overrides, we group the methods fitting the
comment|// same signatures because we get two method definitions from java reflection:
comment|// Mutation.setDurability() : Mutation
comment|//   Delete.setDurability() : Mutation
comment|// Delete.setDurability() : Delete
name|String
name|sig
init|=
name|method
operator|.
name|getName
argument_list|()
decl_stmt|;
for|for
control|(
name|Class
argument_list|<
name|?
argument_list|>
name|param
range|:
name|method
operator|.
name|getParameterTypes
argument_list|()
control|)
block|{
name|sig
operator|+=
name|param
operator|.
name|getName
argument_list|()
expr_stmt|;
block|}
name|Set
argument_list|<
name|Method
argument_list|>
name|sigMethods
init|=
name|methodsBySignature
operator|.
name|get
argument_list|(
name|sig
argument_list|)
decl_stmt|;
if|if
condition|(
name|sigMethods
operator|==
literal|null
condition|)
block|{
name|sigMethods
operator|=
operator|new
name|HashSet
argument_list|<
name|Method
argument_list|>
argument_list|()
expr_stmt|;
name|methodsBySignature
operator|.
name|put
argument_list|(
name|sig
argument_list|,
name|sigMethods
argument_list|)
expr_stmt|;
block|}
name|sigMethods
operator|.
name|add
argument_list|(
name|method
argument_list|)
expr_stmt|;
block|}
block|}
comment|// now iterate over the methods by signatures
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|Method
argument_list|>
argument_list|>
name|e
range|:
name|methodsBySignature
operator|.
name|entrySet
argument_list|()
control|)
block|{
comment|// at least one of method sigs should return the declaring class
name|boolean
name|found
init|=
literal|false
decl_stmt|;
for|for
control|(
name|Method
name|m
range|:
name|e
operator|.
name|getValue
argument_list|()
control|)
block|{
name|found
operator|=
name|clazz
operator|.
name|isAssignableFrom
argument_list|(
name|m
operator|.
name|getReturnType
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|found
condition|)
break|break;
block|}
name|String
name|errorMsg
init|=
literal|"All setXXX()|addXX() methods in "
operator|+
name|clazz
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" should return a "
operator|+
name|clazz
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" object in builder style. "
operator|+
literal|"Offending method:"
operator|+
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|errorMsg
argument_list|,
name|found
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

